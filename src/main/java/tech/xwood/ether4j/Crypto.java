package tech.xwood.ether4j;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

public class Crypto {

  private static class ECDSASignature {

    public final BigInteger r;
    public final BigInteger s;

    public ECDSASignature(final BigInteger r, final BigInteger s) {
      this.r = r;
      this.s = s;
    }

    public boolean isCanonical() {
      return s.compareTo(HALF_CURVE_ORDER) <= 0;
    }

    public ECDSASignature toCanonicalised() {
      return isCanonical() ? this : new ECDSASignature(r, SIGN_CURVE.getN().subtract(s));
    }
  }

  private static class SignatureData {

    public final byte v;
    public final byte[] r;
    public final byte[] s;

    public SignatureData(final byte v, final byte[] r, final byte[] s) {
      this.v = v;
      this.r = r;
      this.s = s;
    }
  }

  private static final X9ECParameters SIGN_CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");

  private static final ECDomainParameters SIGN_CURVE = new ECDomainParameters(
    SIGN_CURVE_PARAMS.getCurve(),
    SIGN_CURVE_PARAMS.getG(),
    SIGN_CURVE_PARAMS.getN(),
    SIGN_CURVE_PARAMS.getH());

  private static final Provider SECURITY_PROVIDER = new BouncyCastleProvider();
  private static final KeyPairGenerator KEY_PAIR_GENERATOR;
  private static final ECGenParameterSpec EC_GEN_PARAMTER_SPEC = new ECGenParameterSpec("secp256k1");
  private static final BigInteger HALF_CURVE_ORDER = SIGN_CURVE_PARAMS.getN().shiftRight(1);
  static {
    try {
      KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance("ECDSA", SECURITY_PROVIDER);
    }
    catch (final NoSuchAlgorithmException e) {
      throw new Error(e);
    }
  }

  public static Quantity createAddress(final Quantity publicKey) {

    final Quantity hash = keccak256(publicKey);
    final String hashHex = hash.toHex();
    final String addressHex = Quantity.HEX_PREFIX + hashHex.substring(hashHex.length() - Quantity.ADDRESS_HEX_LENGH);
    final Quantity address = Quantity.of(addressHex);
    return address;
  }

  public static Quantity createPrivateKey() {
    return createPrivateKey(null);
  }

  public static Quantity createPrivateKey(final byte[] seed) {
    try {
      final SecureRandom random = seed == null ? new SecureRandom() : new SecureRandom(seed);
      KEY_PAIR_GENERATOR.initialize(EC_GEN_PARAMTER_SPEC, random);
      final java.security.KeyPair keyPair = KEY_PAIR_GENERATOR.generateKeyPair();
      final BCECPrivateKey privateKeyBCE = (BCECPrivateKey) keyPair.getPrivate();
      final BigInteger privateKey = privateKeyBCE.getD();
      return Quantity.of(privateKey);
    }
    catch (final InvalidAlgorithmParameterException e) {
      throw new Error(e);
    }
  }

  public static Quantity createPublicKey(final Quantity privateKey) {

    BigInteger privKey = privateKey.toBigInteger();
    if (privKey.bitLength() > SIGN_CURVE.getN().bitLength()) {
      privKey = privKey.mod(SIGN_CURVE.getN());
    }
    final ECPoint point = new FixedPointCombMultiplier().multiply(SIGN_CURVE.getG(), privKey);
    final byte[] encoded = point.getEncoded(false);
    final byte[] publicKeyBytes = Arrays.copyOfRange(encoded, 1, encoded.length);
    return Quantity.of(new BigInteger(1, publicKeyBytes));
  }

  public static Quantity createRawTransaction(final Quantity privateKey, final Quantity publicKey, final Transaction tx) {

    final byte[] txRlp = txToRlp(tx, null);
    final byte[] txRlpHash = keccak256(txRlp);
    final SignatureData signData = sign(privateKey, publicKey, txRlpHash);
    final byte[] rawTxBytes = txToRlp(tx, signData);
    return Quantity.of(rawTxBytes);
  }

  private static ECPoint decompressKey(final BigInteger xBN, final boolean yBit) {
    final X9IntegerConverter x9 = new X9IntegerConverter();
    final byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(SIGN_CURVE.getCurve()));
    compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
    return SIGN_CURVE.getCurve().decodePoint(compEnc);
  }

  public static byte[] keccak256(final byte[] input) {
    final Keccak.DigestKeccak kecc = new Keccak.Digest256();
    kecc.update(input, 0, input.length);
    return kecc.digest();
  }

  public static Quantity keccak256(final Quantity data) {
    return Quantity.of(keccak256(data.toBytes()));
  }

  private static BigInteger recoverFromSignature(final int recId, final ECDSASignature signature, final byte[] data) {

    if (recId < 0) {
      throw new Error("recId must be positive");
    }
    if (signature.r.signum() < 0) {
      throw new Error("r must be positive");
    }
    if (signature.s.signum() < 0) {
      throw new Error("s must be positive");
    }
    if (data == null) {
      throw new Error("message cannot be null");
    }

    final BigInteger n = SIGN_CURVE.getN();
    final BigInteger i = BigInteger.valueOf((long) recId / 2);
    final BigInteger x = signature.r.add(i.multiply(n));
    final BigInteger prime = SecP256K1Curve.q;
    if (x.compareTo(prime) >= 0) {
      return null;
    }
    final ECPoint R = decompressKey(x, (recId & 1) == 1);
    if (!R.multiply(n).isInfinity()) {
      return null;
    }
    final BigInteger e = new BigInteger(1, data);
    final BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
    final BigInteger rInv = signature.r.modInverse(n);
    final BigInteger srInv = rInv.multiply(signature.s).mod(n);
    final BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
    final ECPoint q = ECAlgorithms.sumOfTwoMultiplies(SIGN_CURVE.getG(), eInvrInv, R, srInv);
    final byte[] qBytes = q.getEncoded(false);
    return new BigInteger(1, Arrays.copyOfRange(qBytes, 1, qBytes.length));
  }

  private static ECDSASignature sign(final Quantity privateKey, final byte[] data) {

    final ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
    final ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKey.toBigInteger(), SIGN_CURVE);
    signer.init(true, privKey);
    final BigInteger[] components = signer.generateSignature(data);
    return new ECDSASignature(components[0], components[1]).toCanonicalised();
  }

  private static SignatureData sign(final Quantity privateKey, final Quantity publicKey, final byte[] data) {

    final BigInteger pubKey = publicKey.toBigInteger();
    final ECDSASignature sig = sign(privateKey, data);

    int recId = -1;
    {
      for (int i = 0; i < 4; i++) {
        final BigInteger k = recoverFromSignature(i, sig, data);
        if (k != null && k.equals(pubKey)) {
          recId = i;
          break;
        }
      }
      if (recId == -1) {
        throw new Error("Could not construct a recoverable key. This should never happen.");
      }
    }
    final int headerByte = recId + 27;
    final byte v = (byte) headerByte;
    final byte[] r = toBytesPadded(sig.r, 32);
    final byte[] s = toBytesPadded(sig.s, 32);
    return new SignatureData(v, r, s);
  }

  private static byte[] toBytesPadded(final BigInteger value, final int length) {
    final byte[] result = new byte[length];
    final byte[] bytes = value.toByteArray();

    int bytesLength;
    int srcOffset;
    if (bytes[0] == 0) {
      bytesLength = bytes.length - 1;
      srcOffset = 1;
    }
    else {
      bytesLength = bytes.length;
      srcOffset = 0;
    }
    if (bytesLength > length) {
      throw new Error("Input is too large to put in byte array of size " + length);
    }
    final int destOffset = length - bytesLength;
    System.arraycopy(bytes, srcOffset, result, destOffset, bytesLength);
    return result;
  }

  private static byte[] trimLeadingBytes(final byte[] bytes, final byte b) {
    int offset = 0;
    for (; offset < bytes.length - 1; offset++) {
      if (bytes[offset] != b) {
        break;
      }
    }
    return Arrays.copyOfRange(bytes, offset, bytes.length);
  }

  private static byte[] txToRlp(final Transaction tx, final SignatureData signatureData) {

    final Rlp.TypeList rlpTx = Rlp.createList();

    rlpTx
      .add(Rlp.createString(tx.getNonce()))
      .add(Rlp.createString(tx.getGasPrice()))
      .add(Rlp.createString(tx.getGasLimit()))
      .add(Rlp.createString(tx.getTo()))
      .add(Rlp.createString(tx.getValue()))
      .add(Rlp.createString(tx.getData()));

    if (signatureData != null) {
      rlpTx
        .add(Rlp.createString(signatureData.v))
        .add(Rlp.createString(trimLeadingBytes(signatureData.r, (byte) 0)))
        .add(Rlp.createString(trimLeadingBytes(signatureData.s, (byte) 0)));
    }
    return Rlp.encode(rlpTx);
  }

}
