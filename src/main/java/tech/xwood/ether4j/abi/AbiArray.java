package tech.xwood.ether4j.abi;

import java.util.Arrays;
import tech.xwood.ether4j.Utils;

public class AbiArray extends AbiValue {

  public static class Type extends AbiType {

    static int getValueLength(final String input, final int offset, final AbiType valueType) {

      if (input.length() == offset) {
        return 0;
      }
      else if (valueType instanceof AbiString.Type || valueType instanceof AbiBytesDynamic.Type) {
        return (AbiUint.Type.decode(input, offset, 256).intValue() / Abi.MAX_BYTE_LENGTH) + 2;
      }
      else {
        return 1;
      }
    }

    public static Type of(final AbiType valueType, final int length) {
      return new Type(valueType, length);
    }

    public final AbiType valueType;
    public final int length;

    private Type(final AbiType valueType, final int length) {
      super(String.format("%s[%s]", valueType.name, length));
      Utils.require(length > 0, "Array can't be empty");
      this.length = length;
      this.valueType = valueType;
    }

    @Override
    public AbiArray decode(final String raw) {
      return decode(raw, 0);
    }

    @Override
    public AbiArray decode(final String raw, final int offset) {

      Utils.require(length > 0, "Zero length fixed array is invalid type");
      final AbiValue[] values = new AbiValue[length];
      int currOffset = offset;
      for (int i = 0; i < length; i++) {
        values[i] = valueType.decode(raw, currOffset);
        currOffset += getValueLength(raw, currOffset, valueType) * Abi.MAX_BYTE_LENGTH_FOR_HEX_STRING;
      }
      return new AbiArray(values, this);
    }

    public AbiArray valueOf(final AbiValue... values) {
      return new AbiArray(values, this);
    }

  }

  public static AbiArray of(final AbiValue... values) {
    return Type.of(values[0].type, values.length).valueOf(values);
  }

  public final AbiValue[] values;

  private AbiArray(final AbiValue[] values, final AbiArray.Type type) {
    super(type);
    Utils.require(values.length == type.length, "Data length and type are not equals");
    this.values = values;
  }

  @Override
  public void encodeTo(final StringBuilder dest) {
    for (final AbiValue value : values) {
      value.encodeTo(dest);
    }
  }

  @Override
  protected boolean equalsImpl(final AbiValue other) {
    return Arrays.equals(values, ((AbiArray) other).values);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }

}