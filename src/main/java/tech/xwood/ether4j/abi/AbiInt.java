package tech.xwood.ether4j.abi;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AbiInt extends AbiValue {

  public static class Type extends AbiType {

    private static Map<Integer, AbiInt.Type> INSTANCES = new HashMap<>(Abi.MAX_BIT_LENGTH / 8);
    static {
      for (int bitLength = 8; bitLength <= Abi.MAX_BIT_LENGTH; bitLength += 8) {
        INSTANCES.put(bitLength, new Type(bitLength));
      }
    }

    public static Type of(final int bitLength) {
      return INSTANCES.get(bitLength);
    }

    public final int bitLength;

    private Type(final int bitLength) {
      super("int" + bitLength);
      AbiUtils.require(bitLength % 8 == 0 && bitLength > 0 && bitLength <= Abi.MAX_BIT_LENGTH, "Bitsize must be 8 bit aligned, and in range 0 < bitSize <= 256");
      this.bitLength = bitLength;
    }

    @Override
    public AbiInt decode(final String raw) {
      return this.decode(raw, 0);
    }

    @Override
    public AbiInt decode(final String raw, final int offset) {
      final String input = raw.substring(offset, offset + Abi.MAX_BYTE_LENGTH_FOR_HEX_STRING);
      final byte[] inputBytes = Abi.hexToBytes(input);
      final int byteLength = this.bitLength >> 3;
      final byte[] valueBytes = new byte[byteLength + 1];
      valueBytes[0] = inputBytes[0]; // take MSB as sign bit
      final int valueOffset = Abi.MAX_BYTE_LENGTH - byteLength;
      System.arraycopy(inputBytes, valueOffset, valueBytes, 1, byteLength);
      final BigInteger value = new BigInteger(valueBytes);
      return new AbiInt(value, this);
    }

    public AbiInt valueOf(final BigInteger value) {
      return new AbiInt(value, this);
    }

    public AbiInt valueOf(final long value) {
      return new AbiInt(BigInteger.valueOf(value), this);
    }

    public AbiInt valueOf(final String hexWithoutPrefix) {
      return new AbiInt(new BigInteger(hexWithoutPrefix, 16), this);
    }

  }

  static void encodeTo(final StringBuilder dest, final BigInteger value) {
    final byte[] rawValue = value.toByteArray();
    final byte paddingValue = value.signum() == -1 ? (byte) 0xff : 0;
    final byte[] paddedRawValue = new byte[Abi.MAX_BYTE_LENGTH];
    if (paddingValue != 0) {
      for (int i = 0; i < paddedRawValue.length; i++) {
        paddedRawValue[i] = paddingValue;
      }
    }
    System.arraycopy(rawValue, 0, paddedRawValue, Abi.MAX_BYTE_LENGTH - rawValue.length, rawValue.length);
    Abi.hexTo(dest, paddedRawValue);
  }

  public static AbiInt of(final int bitLength, final BigInteger value) {
    return Type.of(bitLength).valueOf(value);
  }

  public static AbiInt of(final int bitLength, final long value) {
    return Type.of(bitLength).valueOf(value);
  }

  public static AbiInt of(final int bitLength, final String hexWithoutPrefix) {
    return Type.of(bitLength).valueOf(hexWithoutPrefix);
  }

  public final BigInteger value;

  private AbiInt(final BigInteger value, final AbiInt.Type type) {
    super(type);
    AbiUtils.require(value.bitLength() <= type.bitLength, "Wrong bit length");
    this.value = value;
  }

  @Override
  public void encodeTo(final StringBuilder dest) {
    encodeTo(dest, this.value);
  }

  @Override
  protected boolean equalsImpl(final AbiValue other) {
    return Objects.equals(this.value, ((AbiInt) other).value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.value);
  }

}