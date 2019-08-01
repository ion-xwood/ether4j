package tech.xwood.ether4j.abi;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAbiAddress {

  @Test
  public void testDecode() {

    Assert.assertEquals(
      AbiAddress.Type.get().decode("000000000000000000000000be5422d15f39373eb0a97ff8c10fbd0e40e29338"),
      AbiAddress.of("be5422d15f39373eb0a97ff8c10fbd0e40e29338"));
  }

  @Test
  public void testEncode() {
    Assert.assertEquals(
      "000000000000000000000000be5422d15f39373eb0a97ff8c10fbd0e40e29338",
      AbiAddress.of("be5422d15f39373eb0a97ff8c10fbd0e40e29338").encode());
  }

  @Test
  public void testType() {
    Assert.assertEquals(AbiAddress.Type.get().name, "address");
  }

}
