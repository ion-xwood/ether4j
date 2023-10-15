package tech.xwood.ether4j.abi;

import java.util.Objects;

public abstract class AbiType {

  public final String name;

  public AbiType(final String name) {
    this.name = name;
  }

  public abstract AbiValue decode(final String raw);

  public abstract AbiValue decode(final String raw, final int offset);

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof AbiType)) {
      return false;
    }
    return Objects.equals(this.name, ((AbiType) obj).name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name);
  }

}