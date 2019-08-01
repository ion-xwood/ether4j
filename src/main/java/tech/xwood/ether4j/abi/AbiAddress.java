package tech.xwood.ether4j.abi;

import java.math.BigInteger;
import java.util.Objects;
import tech.xwood.ether4j.Quantity;

public class AbiAddress extends AbiValue {

  public static class Type extends AbiType {

    private static final AbiAddress.Type INSTANCE = new Type();

    public static Type get() {
      return INSTANCE;
    }

    private Type() {
      super("address");
    }

    @Override
    public AbiAddress decode(final String raw) {
      return decode(raw, 0);
    }

    @Override
    public AbiAddress decode(final String raw, final int offset) {
      return new AbiAddress(AbiUint.Type.decode(raw, offset, 160));
    }

    public AbiAddress valueOf(final BigInteger value) {
      return new AbiAddress(value);
    }

    public AbiAddress valueOf(final long value) {
      return new AbiAddress(BigInteger.valueOf(value));
    }

    public AbiAddress valueOf(final String hexWithoutPrefix) {
      return new AbiAddress(new BigInteger(hexWithoutPrefix, 16));
    }

  }

  public static AbiAddress of(final BigInteger value) {
    return Type.INSTANCE.valueOf(value);
  }

  public static AbiAddress of(final long value) {
    return Type.INSTANCE.valueOf(value);
  }

  public static AbiAddress of(final Quantity quantity) {
    return of(quantity.toBigInteger());
  }

  public static AbiAddress of(final String hexWithoutPrefix) {
    return Type.INSTANCE.valueOf(hexWithoutPrefix);
  }

  public final BigInteger value;

  private AbiAddress(final BigInteger value) {
    super(Type.INSTANCE);
    this.value = value;
  }

  @Override
  public void encodeTo(final StringBuilder dest) {
    AbiUint.encodeTo(dest, value);
  }

  @Override
  protected boolean equalsImpl(final AbiValue other) {
    return Objects.equals(value, ((AbiAddress) other).value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  public Quantity toQuantity() {
    return Quantity.of(value);
  }

  @Override
  public String toString() {
    return "0x" + value.toString(16);
  }

}