package tech.xwood.ether4j.abi;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AbiArrayDynamicTest {

  @Test
  public void testDecode() {
    Assert.assertEquals(
      AbiArrayDynamic.Type.of(AbiUint.Type.of(256))
        .decode(
          "0000000000000000000000000000000000000000000000000000000000000000"),
      AbiArrayDynamic.empty(AbiUint.Type.of(256)));
    Assert.assertEquals(
      AbiArrayDynamic.Type.of(AbiUint.Type.of(256))
        .decode(
          "000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000"
            + "0000000000000000000a0000000000000000000000000000000000000000000000007fffffffffffffff"),
      AbiArrayDynamic.of(
        AbiUint.of(256, 10),
        AbiUint.of(256, Long.MAX_VALUE)));
    Assert.assertEquals(
      AbiArrayDynamic.Type.of(AbiString.Type.get())
        .decode(
          "000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000"
            + "0000000000000000000d48656c6c6f2c20776f726c64210000000000000000000000000000000000000000000000000000000000"
            + "0000000000000000000000000000000000000000000d776f726c64212048656c6c6f2c0000000000000000000000000000000000"
            + "0000"),
      AbiArrayDynamic.of(
        AbiString.of("Hello, world!"),
        AbiString.of("world! Hello,")));
  }

  @Test
  public void testEncode() {
    Assert.assertEquals(
      "0000000000000000000000000000000000000000000000000000000000000003000000000000000000000000000000000000000000000000"
        + "000000000000000100000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000"
        + "000000000000000000000000000000000003",
      AbiArrayDynamic.of(
        AbiUint.of(256, 1),
        AbiUint.of(256, 2),
        AbiUint.of(256, 3)).encode());
    Assert.assertEquals(
      "0000000000000000000000000000000000000000000000000000000000000000",
      AbiArrayDynamic.empty(AbiUint.Type.of(256)).encode());
  }

  @Test
  public void testType() {
    Assert.assertEquals(AbiArrayDynamic.Type.of(AbiUint.Type.of(64)).name, "uint64[]");
  }

}
