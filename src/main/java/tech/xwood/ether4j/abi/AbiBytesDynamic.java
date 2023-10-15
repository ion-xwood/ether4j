package tech.xwood.ether4j.abi;

import java.math.BigInteger;
import java.util.Arrays;

public class AbiBytesDynamic extends AbiValue {

  public static class Type extends AbiType {

    private static final AbiBytesDynamic.Type INSTANCE = new Type();

    static byte[] decodeBytes(final String raw, final int offset) {
      final int encodedLength = AbiUint.Type.decode(raw, offset, 256).intValue();
      final int hexStringEncodedLength = encodedLength << 1;
      final int valueOffset = offset + Abi.MAX_BYTE_LENGTH_FOR_HEX_STRING;
      final String data = raw.substring(valueOffset, valueOffset + hexStringEncodedLength);
      return Abi.hexToBytes(data);
    }

    public static AbiBytesDynamic.Type get() {
      return Type.INSTANCE;
    }

    private Type() {
      super("bytes");
    }

    @Override
    public AbiBytesDynamic decode(final String raw) {
      return decode(raw, 0);
    }

    @Override
    public AbiBytesDynamic decode(final String raw, final int offset) {
      final byte[] bytes = decodeBytes(raw, offset);
      return new AbiBytesDynamic(bytes);
    }

    public AbiBytesDynamic valueOf(final byte[] value) {
      return new AbiBytesDynamic(value);
    }

  }

  static void encodeBytesTo(final StringBuilder dest, final byte[] value) {
    AbiUint.encodeTo(dest, BigInteger.valueOf(value.length));
    Abi.hexTo(dest, AbiBytes.encodeBytes(value));
  }

  public static AbiBytesDynamic of(final byte[] value) {
    return Type.INSTANCE.valueOf(value);
  }

  public final byte[] value;

  private AbiBytesDynamic(final byte[] value) {
    super(Type.INSTANCE);
    this.value = value;
  }

  @Override
  public void encodeTo(final StringBuilder dest) {
    encodeBytesTo(dest, value);
  }

  @Override
  protected boolean equalsImpl(final AbiValue other) {
    return Arrays.equals(value, ((AbiBytesDynamic) other).value);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }

}