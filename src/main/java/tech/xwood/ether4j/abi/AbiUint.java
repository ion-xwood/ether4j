package tech.xwood.ether4j.abi;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AbiUint extends AbiValue {

  public static class Type extends AbiType {

    private static Map<Integer, AbiUint.Type> INSTANCES = new HashMap<>(Abi.MAX_BIT_LENGTH / 8);
    static {
      for (int bitLength = 8; bitLength <= Abi.MAX_BIT_LENGTH; bitLength += 8) {
        INSTANCES.put(bitLength, new Type(bitLength));
      }
    }

    static BigInteger decode(final String raw, final int offset, final int bitLength) {
      final String input = raw.substring(offset, offset + Abi.MAX_BYTE_LENGTH_FOR_HEX_STRING);
      final byte[] inputBytes = Abi.hexToBytes(input);
      final int byteLength = bitLength >> 3;
      final byte[] valueBytes = new byte[byteLength + 1];
      final int valueOffset = Abi.MAX_BYTE_LENGTH - byteLength;
      System.arraycopy(inputBytes, valueOffset, valueBytes, 1, byteLength);
      return new BigInteger(valueBytes);
    }

    public static AbiUint.Type of(final int bitLength) {
      return INSTANCES.get(bitLength);
    }

    public final int bitLength;

    private Type(final int bitLength) {
      super("uint" + bitLength);
      AbiUtils.require(bitLength % 8 == 0 && bitLength > 0 && bitLength <= Abi.MAX_BIT_LENGTH, "Bitsize must be 8 bit aligned, and in range 0 < bitSize <= 256");
      this.bitLength = bitLength;
    }

    @Override
    public AbiUint decode(final String raw) {
      return this.decode(raw, 0);
    }

    @Override
    public AbiUint decode(final String raw, final int offset) {
      return new AbiUint(decode(raw, offset, this.bitLength), this);
    }

    public AbiUint valueOf(final BigInteger value) {
      return new AbiUint(value, this);
    }

    public AbiUint valueOf(final long value) {
      return new AbiUint(BigInteger.valueOf(value), this);
    }

    public AbiUint valueOf(final String hexWithoutPrefix) {
      return new AbiUint(new BigInteger(hexWithoutPrefix, 16), this);
    }

  }

  static String encode(final BigInteger value) {
    final StringBuilder dest = new StringBuilder();
    encodeTo(dest, value);
    return dest.toString();
  }

  static void encodeTo(final StringBuilder dest, final BigInteger value) {
    final byte[] rawValue;
    {
      if (value.bitLength() == Abi.MAX_BIT_LENGTH) {
        final byte[] byteArray = new byte[Abi.MAX_BYTE_LENGTH];
        System.arraycopy(value.toByteArray(), 1, byteArray, 0, Abi.MAX_BYTE_LENGTH);
        rawValue = byteArray;
      }
      else {
        rawValue = value.toByteArray();
      }
    }
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

  public static AbiUint of(final int bitLength, final BigInteger value) {
    return Type.of(bitLength).valueOf(value);
  }

  public static AbiUint of(final int bitLength, final long value) {
    return Type.of(bitLength).valueOf(value);
  }

  public static AbiUint of(final int bitLength, final String hexWithoutPrefix) {
    return Type.of(bitLength).valueOf(hexWithoutPrefix);
  }

  public final BigInteger value;

  private AbiUint(final BigInteger value, final AbiUint.Type type) {
    super(type);
    AbiUtils.require(value.bitLength() <= type.bitLength, "Wrong bit length");
    AbiUtils.require(value.signum() != -1, "Value must be unsigned");
    this.value = value;
  }

  @Override
  public void encodeTo(final StringBuilder dest) {
    encodeTo(dest, this.value);
  }

  @Override
  protected boolean equalsImpl(final AbiValue other) {
    return Objects.equals(this.value, ((AbiUint) other).value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.value);
  }

  @Override
  public String toString() {
    return this.value.toString();
  }

}