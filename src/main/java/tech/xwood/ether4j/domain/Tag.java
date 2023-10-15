package tech.xwood.ether4j.domain;

public enum Tag {

    /**
     * for the earliest/genesis block
     */
    EARLIEST("earliest"),
    /**
     * for the latest mined block
     */
    LATEST("latest"),
    /**
     * for the pending state/transactions
     */
    PENDING("pending");

  private final String name;

  Tag(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

}
