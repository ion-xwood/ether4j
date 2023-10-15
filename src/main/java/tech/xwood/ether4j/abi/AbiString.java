package tech.xwood.ether4j.abi;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class AbiString extends AbiValue {

  public static class Type extends AbiType {

    private static final AbiString.Type INSTANCE = new Type();

    public static AbiString.Type get() {
      return Type.INSTANCE;
    }

    private Type() {
      super("string");
    }

    @Override
    public AbiString decode(final String raw) {
      return decode(raw, 0);
    }

    @Override
    public AbiString decode(final String raw, final int offset) {
      final byte[] bytes = AbiBytesDynamic.Type.decodeBytes(raw, offset);
      return new AbiString(new String(bytes, StandardCharsets.UTF_8));
    }

    public AbiString valueOf(final String value) {
      return new AbiString(value);
    }

  }

  public static AbiString of(final String value) {
    return Type.INSTANCE.valueOf(value);
  }

  public final String value;

  private AbiString(final String value) {
    super(Type.INSTANCE);
    this.value = value;
  }

  @Override
  public void encodeTo(final StringBuilder dest) {
    AbiBytesDynamic.encodeBytesTo(dest, value.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  protected boolean equalsImpl(final AbiValue other) {
    return Objects.equals(value, ((AbiString) other).value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }

}