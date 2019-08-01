package tech.xwood.ether4j.abi;

import org.testng.Assert;
import org.testng.annotations.Test;
import tech.xwood.ether4j.Error;

public class TestAbiUint {

  @Test
  public void testDecode() {

    Assert.assertEquals(
      AbiUint.Type.of(64).decode("0000000000000000000000000000000000000000000000000000000000000000"),
      AbiUint.of(64, 0));

    Assert.assertEquals(
      AbiUint.Type.of(64).decode("0000000000000000000000000000000000000000000000007fffffffffffffff"),
      AbiUint.of(64, Long.MAX_VALUE));

    Assert.assertEquals(
      AbiUint.Type.of(64).decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
      AbiUint.of(64, "0ffffffffffffffff"));
  }

  @Test
  public void testEncode() {

    Assert.assertEquals(
      "0000000000000000000000000000000000000000000000000000000000000000",
      AbiUint.of(64, 0).encode());

    Assert.assertEquals(
      "0000000000000000000000000000000000000000000000007fffffffffffffff",
      AbiUint.of(64, Long.MAX_VALUE).encode());

    Assert.assertEquals(
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      AbiUint.of(256, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff").encode());

    Assert.assertEquals(
      "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe",
      AbiUint.of(256, "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe").encode());
  }

  @Test(expectedExceptions = Error.class)
  public void testInvalidEncode() {
    AbiUint.of(64, -1L);
  }

  @Test(expectedExceptions = Error.class)
  public void testTooLargeEncode() {
    AbiUint.of(256, "10000000000000000000000000000000000000000000000000000000000000000");
  }

  @Test
  public void testType() {
    Assert.assertEquals(AbiUint.Type.of(64).name, "uint64");
    Assert.assertEquals(AbiUint.Type.of(64), AbiUint.Type.of(64));
  }

}
