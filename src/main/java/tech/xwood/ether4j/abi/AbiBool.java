package tech.xwood.ether4j.abi;

import java.math.BigInteger;
import java.util.Objects;

public class AbiBool extends AbiValue {

  public static class Type extends AbiType {

    private static final Type INSTANCE = new Type();

    public static AbiBool.Type get() {
      return INSTANCE;
    }

    private Type() {
      super("bool");
    }

    @Override
    public AbiBool decode(final String raw) {
      return decode(raw, 0);
    }

    @Override
    public AbiBool decode(final String raw, final int offset) {
      final String input = raw.substring(offset, offset + Abi.MAX_BYTE_LENGTH_FOR_HEX_STRING);
      final boolean value = new BigInteger(input, 16).equals(BigInteger.ONE);
      return new AbiBool(value);
    }

    public AbiBool valueOf(final boolean value) {
      return new AbiBool(value);
    }

  }

  public static AbiBool of(final boolean value) {
    return Type.INSTANCE.valueOf(value);
  }

  public final boolean value;

  private AbiBool(final boolean value) {
    super(Type.INSTANCE);
    this.value = value;
  }

  @Override
  public void encodeTo(final StringBuilder dest) {
    final byte[] rawValue = new byte[Abi.MAX_BYTE_LENGTH];
    rawValue[rawValue.length - 1] = value ? (byte) 1 : (byte) 0;
    Abi.hexTo(dest, rawValue);
  }

  @Override
  protected boolean equalsImpl(final AbiValue other) {
    return Objects.equals(value, ((AbiBool) other).value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

}