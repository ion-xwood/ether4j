package tech.xwood.ether4j;

import java.util.Objects;

public class Account {

  public static Account generate() {
    return generate(null);
  }

  public static Account generate(final byte[] seed) {
    final Quantity privateKey = Crypto.createPrivateKey(seed);
    return new Account(privateKey);
  }

  public static Account of(final Quantity privateKey) {
    return new Account(privateKey);
  }

  private final Quantity privateKey;
  private final Quantity publicKey;
  private final Quantity address;

  private Account(final Quantity privateKey) {
    this.privateKey = privateKey;
    publicKey = Crypto.createPublicKey(privateKey);
    address = Crypto.createAddress(publicKey);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof Account)) {
      return false;
    }
    final Account other = (Account) obj;
    return Objects.equals(address, other.address)
      && Objects.equals(privateKey, other.privateKey)
      && Objects.equals(publicKey, other.publicKey);
  }

  public Quantity getAddress() {
    return address;
  }

  public Quantity getPrivateKey() {
    return privateKey;
  }

  public Quantity getPublicKey() {
    return publicKey;
  }

  @Override
  public int hashCode() {
    return Objects.hash(address, privateKey, publicKey);
  }

  @Override
  public String toString() {
    return Utils.toJson(this, true);
  }

}
