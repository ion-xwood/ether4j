package tech.xwood.ether4j.abi;

import java.math.BigInteger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AbiIntTest {

  @Test
  public void testDecode() {
    Assert.assertEquals(
      AbiInt.Type.of(64).decode("0000000000000000000000000000000000000000000000000000000000000000"),
      AbiInt.of(64, 0));
    Assert.assertEquals(
      AbiInt.Type.of(64).decode("0000000000000000000000000000000000000000000000007fffffffffffffff"),
      AbiInt.of(64, Long.MAX_VALUE));
    Assert.assertEquals(
      AbiInt.Type.of(64).decode("fffffffffffffffffffffffffffffffffffffffffffffff88000000000000000"),
      AbiInt.of(64, Long.MIN_VALUE));
    Assert.assertEquals(
      AbiInt.Type.of(64).decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
      AbiInt.of(64, -1L));
    Assert.assertEquals(
      AbiInt.Type.of(256).decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
      AbiInt.of(256, -1L));
  }

  @Test
  public void testEncode() {
    Assert.assertEquals(
      "0000000000000000000000000000000000000000000000000000000000000000",
      AbiInt.of(64, BigInteger.ZERO).encode());
    Assert.assertEquals(
      "0000000000000000000000000000000000000000000000007fffffffffffffff",
      AbiInt.of(64, Long.MAX_VALUE).encode());
    Assert.assertEquals(
      "ffffffffffffffffffffffffffffffffffffffffffffffff8000000000000000",
      AbiInt.of(64, Long.MIN_VALUE).encode());
    Assert.assertEquals(
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      AbiInt.of(64, -1L).encode());
  }

  @Test
  public void testType() {
    Assert.assertEquals(AbiInt.Type.of(64).name, "int64");
    Assert.assertEquals(AbiInt.Type.of(64), AbiInt.Type.of(64));
  }

}
