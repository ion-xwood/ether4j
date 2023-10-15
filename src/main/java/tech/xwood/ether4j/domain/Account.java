package tech.xwood.ether4j.domain;

import java.util.Objects;
import tech.xwood.ether4j.json.JsonCodec;

public class Account {

  public static Account generate() {
    return generate(null);
  }

  public static Account generate(final byte[] seed) {
    return new Account(Crypto.createPrivateKey(seed));
  }

  public static Account of(final Quantity privateKey) {
    return new Account(privateKey);
  }

  private final Quantity privateKey;

  private final Quantity publicKey;

  private final Quantity address;

  private Account(final Quantity privateKey) {
    this.privateKey = privateKey;
    this.publicKey = Crypto.createPublicKey(privateKey);
    this.address = Crypto.createAddress(this.publicKey);
  }

  public Quantity address() {
    return this.address;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof final Account other)) {
      return false;
    }
    return Objects.equals(this.address, other.address)
      && Objects.equals(this.privateKey, other.privateKey)
      && Objects.equals(this.publicKey, other.publicKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.address, this.privateKey, this.publicKey);
  }

  public Quantity privateKey() {
    return this.privateKey;
  }

  public Quantity publicKey() {
    return this.publicKey;
  }

  @Override
  public String toString() {
    return JsonCodec.toJson(this, true);
  }

}
