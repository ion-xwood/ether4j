package tech.xwood.ether4j.abi;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAbiString {

  @Test
  public void testDecode() {

    Assert.assertEquals(
      AbiString.Type.get().decode(
        "000000000000000000000000000000000000000000000000000000000000000d48656c6c6f2c20776f726c642100000000000000000000"
          + "000000000000000000"),
      AbiString.of("Hello, world!"));
  }

  @Test
  public void testEncode() {
    Assert.assertEquals(
      "000000000000000000000000000000000000000000000000000000000000000d48656c6c6f2c20776f726c64210000000000000000000000"
        + "0000000000000000",
      AbiString.of("Hello, world!").encode());
  }

  @Test
  public void testType() {
    Assert.assertEquals(AbiString.Type.get().name, "string");
  }

}
