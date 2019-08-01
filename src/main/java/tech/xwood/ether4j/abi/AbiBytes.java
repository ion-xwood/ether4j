package tech.xwood.ether4j.abi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import tech.xwood.ether4j.Utils;

public class AbiBytes extends AbiValue {

  public static class Type extends AbiType {

    private static Map<Integer, AbiBytes.Type> INSTANCES = new HashMap<>(32);

    static {
      for (int byteSize = 1; byteSize <= 32; byteSize++) {
        INSTANCES.put(byteSize, new Type(byteSize));
      }
    }

    public static Type of(final int byteSize) {
      return INSTANCES.get(byteSize);
    }

    public final int byteSize;

    private Type(final int byteSize) {
      super("bytes" + byteSize);
      Utils.require(byteSize > 0 && byteSize <= 32, "Input byte array must be in range 0 < M <= 32");
      this.byteSize = byteSize;
    }

    @Override
    public AbiBytes decode(final String raw) {
      return decode(raw, 0);
    }

    @Override
    public AbiBytes decode(final String raw, final int offset) {
      final int hexStringLength = byteSize << 1;
      final String input = raw.substring(offset, hexStringLength);
      final byte[] inputBytes = Abi.hexToBytes(input);
      return new AbiBytes(inputBytes, this);
    }

    public AbiBytes valueOf(final byte[] value) {
      return new AbiBytes(value, this);
    }

  }

  static byte[] encodeBytes(final byte[] data) {

    final int length = data.length;
    final int mod = length % Abi.MAX_BYTE_LENGTH;
    if (mod == 0) {
      return data;
    }
    final int padding = Abi.MAX_BYTE_LENGTH - mod;
    final byte[] dest = new byte[length + padding];
    System.arraycopy(data, 0, dest, 0, length);
    return dest;
  }

  public static AbiBytes of(final byte[] value) {
    return Type.of(value.length).valueOf(value);
  }

  public final byte[] value;

  private AbiBytes(final byte[] value, final AbiBytes.Type type) {
    super(type);
    Utils.require(value.length == type.byteSize, "Input byte array must be in range 0 < M <= 32 and length must match type");
    this.value = value;
  }

  @Override
  public void encodeTo(final StringBuilder dest) {
    Abi.hexTo(dest, encodeBytes(value));
  }

  @Override
  protected boolean equalsImpl(final AbiValue other) {
    return Arrays.equals(value, ((AbiBytes) other).value);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }

}