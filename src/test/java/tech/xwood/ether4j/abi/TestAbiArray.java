package tech.xwood.ether4j.abi;

import org.testng.Assert;
import org.testng.annotations.Test;
import tech.xwood.ether4j.Error;

public class TestAbiArray {

  @Test
  public void testDecode() {

    Assert.assertEquals(
      AbiArray.Type.of(AbiUint.Type.of(256), 2)
        .decode(
          "000000000000000000000000000000000000000000000000000000000000000a00000000000000000000000000000000000000000000"
            + "00007fffffffffffffff"),
      AbiArray.of(
        AbiUint.of(256, 10),
        AbiUint.of(256, Long.MAX_VALUE)));

    Assert.assertEquals(
      AbiArray.Type.of(AbiString.Type.get(), 2)
        .decode(
          "000000000000000000000000000000000000000000000000000000000000000d48656c6c6f2c20776f726c6421000000000000000000"
            + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000d776f726c64212048656c"
            + "6c6f2c00000000000000000000000000000000000000"),
      AbiArray.of(
        AbiString.of("Hello, world!"),
        AbiString.of("world! Hello,")));
  }

  @Test(expectedExceptions = Error.class)
  public void testDecodeEmpty() {

    Assert.assertEquals(
      AbiArray.Type.of(AbiUint.Type.of(256), 0)
        .decode(
          "0000000000000000000000000000000000000000000000000000000000000000"),
      AbiArray.of(AbiUint.of(256, 1)));
  }

  @Test
  public void testEncode() {
    Assert.assertEquals(
      "0000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000"
        + "0000000000000001",
      AbiArray.of(AbiBool.of(true), AbiBool.of(true)).encode());
  }

  @Test
  public void testType() {
    Assert.assertEquals(AbiArray.Type.of(AbiUint.Type.of(64), 10).name, "uint64[10]");
  }

}
