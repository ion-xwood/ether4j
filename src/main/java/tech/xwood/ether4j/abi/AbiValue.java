package tech.xwood.ether4j.abi;

public abstract class AbiValue {

  public final AbiType type;

  public AbiValue(final AbiType type) {
    this.type = type;
  }

  public String encode() {
    final StringBuilder dest = new StringBuilder();
    encodeTo(dest);
    return dest.toString();
  }

  public abstract void encodeTo(StringBuilder dest);

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    return equalsImpl((AbiValue) obj);
  }

  protected abstract boolean equalsImpl(AbiValue other);

  @Override
  public abstract int hashCode();

}