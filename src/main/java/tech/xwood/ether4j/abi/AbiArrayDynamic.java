package tech.xwood.ether4j.abi;

import java.math.BigInteger;
import java.util.Arrays;

public class AbiArrayDynamic extends AbiValue {

  public static class Type extends AbiType {

    public static AbiArrayDynamic.Type of(final AbiType valueType) {
      return new Type(valueType);
    }

    public final AbiType valueType;

    private Type(final AbiType valueType) {
      super(valueType.name + "[]");
      this.valueType = valueType;
    }

    @Override
    public AbiArrayDynamic decode(final String raw) {
      return decode(raw, 0);
    }

    @Override
    public AbiArrayDynamic decode(final String raw, final int offset) {
      final int length = AbiUint.Type.decode(raw, offset, 256).intValue();
      if (length == 0) {
        return new AbiArrayDynamic(new AbiValue[0], this);
      }
      final AbiValue[] values = new AbiValue[length];
      int currOffset = offset + Abi.MAX_BYTE_LENGTH_FOR_HEX_STRING;
      for (int i = 0; i < length; i++) {
        values[i] = valueType.decode(raw, currOffset);
        currOffset += AbiArray.Type.getValueLength(raw, currOffset, valueType) * Abi.MAX_BYTE_LENGTH_FOR_HEX_STRING;
      }
      return new AbiArrayDynamic(values, this);
    }

    public AbiArrayDynamic valueOf(final AbiValue... values) {
      return new AbiArrayDynamic(values, this);
    }

  }

  public static AbiArrayDynamic empty(final AbiType valueType) {
    return Type.of(valueType).valueOf();
  }

  public static AbiArrayDynamic of(final AbiValue... values) {
    return Type.of(values[0].type).valueOf(values);
  }

  public final AbiValue[] values;

  private AbiArrayDynamic(final AbiValue[] values, final AbiArrayDynamic.Type type) {
    super(type);
    this.values = values;
  }

  @Override
  public void encodeTo(final StringBuilder dest) {
    AbiUint.encodeTo(dest, BigInteger.valueOf(values.length));
    for (final AbiValue value : values) {
      value.encodeTo(dest);
    }
  }

  @Override
  protected boolean equalsImpl(final AbiValue other) {
    return Arrays.equals(values, ((AbiArrayDynamic) other).values);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }

}