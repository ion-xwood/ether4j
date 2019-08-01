package tech.xwood.ether4j;

import java.math.BigDecimal;

public enum Unit {

    WEI("wei", 0),
    KWEI("kwei", 3),
    MWEI("mwei", 6),
    GWEI("gwei", 9),
    SZABO("szabo", 12),
    FINNEY("finney", 15),
    ETHER("ether", 18),
    KETHER("kether", 21),
    METHER("mether", 24),
    GETHER("gether", 27);

  public static Unit fromString(final String name) {
    if (name != null) {
      for (final Unit unit : Unit.values()) {
        if (name.equalsIgnoreCase(unit.name)) {
          return unit;
        }
      }
    }
    return Unit.valueOf(name);
  }

  private final String name;
  private final BigDecimal weiFactor;

  Unit(final String name, final int factor) {
    this.name = name;
    weiFactor = BigDecimal.TEN.pow(factor);
  }

  public BigDecimal getWeiFactor() {
    return weiFactor;
  }

  @Override
  public String toString() {
    return name;
  }

}
