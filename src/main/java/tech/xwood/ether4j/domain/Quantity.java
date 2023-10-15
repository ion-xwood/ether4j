package tech.xwood.ether4j.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class Quantity {

  public static final int ADDRESS_HEX_LENGH = 40;

  public static final int HASH_HEX_LENGH = 64;

  public static final String HEX_PREFIX = "0x";

  public static Quantity of(final BigDecimal value, final Unit unit) {
    final BigInteger bigInt = value.multiply(unit.getWeiFactor()).toBigInteger();
    return new Quantity(bigInt, 0);
  }

  public static Quantity of(final BigInteger value) {
    return new Quantity(value, 0);
  }

  public static Quantity of(final byte[] bytes) {
    return new Quantity(new BigInteger(1, bytes), 0);
  }

  public static Quantity of(final double value, final Unit unit) {
    return of(new BigDecimal(value), unit);
  }

  public static Quantity of(final long value) {
    return new Quantity(BigInteger.valueOf(value), 0);
  }

  public static Quantity of(final long value, final Unit unit) {
    return of(new BigDecimal(value), unit);
  }

  @JsonCreator
  public static Quantity of(final String hex) {
    if (HEX_PREFIX.equals(hex)) {
      return null;
    }
    if (!validateHex(hex)) {
      throw new Error("Value must be in format 0x[1-9]+[0-9]* or 0x0");
    }
    try {
      final BigInteger value = new BigInteger(hex.substring(2), 16);
      return new Quantity(value, hex.length() - 2);
    }
    catch (final NumberFormatException e) {
      throw new Error("Negative ", e);
    }
  }

  public static Quantity ofHexWithoutPrefix(final String hexWithoutPrefix) {
    return Quantity.of(HEX_PREFIX + hexWithoutPrefix);
  }

  private static String strRepeat(final char value, final int n) {
    return new String(new char[n]).replace("\0", String.valueOf(value));
  }

  private static boolean validateHex(final String value) {
    return value != null && value.length() >= 3 && value.startsWith(HEX_PREFIX);
  }

  private final BigInteger value;

  private final int hexLength;

  private Quantity(final BigInteger value, final int hexLength) {
    this.value = value;
    this.hexLength = hexLength;
  }

  public Quantity appendHex(final String hexWithoutPrefix) {
    return Quantity.of(this.toHex() + hexWithoutPrefix);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof final Quantity other)) {
      return false;
    }
    return (this.value == other.value) || (this.value != null && this.value.equals(other.value));
  }

  @Override
  public int hashCode() {
    return this.value.hashCode();
  }

  public BigInteger toBigInteger() {
    return this.value;
  }

  public byte[] toBytes() {
    if (this.value.signum() < 1) {
      return new byte[] {};
    }
    final byte[] bytes = this.value.toByteArray();
    return bytes[0] == 0 ? Arrays.copyOfRange(bytes, 1, bytes.length) : bytes;
  }

  public BigDecimal toDecimal(final Unit unit) {
    return new BigDecimal(this.value)
      .divide(unit.getWeiFactor());
  }

  @JsonValue
  public String toHex() {
    return this.toHex(this.hexLength);
  }

  public String toHex(final int size) {
    return HEX_PREFIX + this.toHexWithoutPrefix(size);
  }

  public String toHexAddress() {
    return this.toHex(ADDRESS_HEX_LENGH);
  }

  public String toHexHash() {
    return this.toHex(HASH_HEX_LENGH);
  }

  public String toHexWithoutPrefix() {
    return this.toHexWithoutPrefix(this.hexLength);
  }

  public String toHexWithoutPrefix(final int size) {
    if (this.value.signum() < 0) {
      throw new Error("Negative values are not supported");
    }
    String result = this.value.toString(16);
    if (size > 0) {
      final int length = result.length();
      if (length > size) {
        throw new Error("Value " + result + "is larger then length " + size);
      }
      if (length < size) {
        result = strRepeat('0', size - length) + result;
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return this.toHex();
  }

}
