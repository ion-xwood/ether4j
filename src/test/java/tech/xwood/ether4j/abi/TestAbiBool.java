package tech.xwood.ether4j.abi;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAbiBool {

  @Test
  public void testDecode() {

    Assert.assertEquals(
      AbiBool.Type.get().decode("0000000000000000000000000000000000000000000000000000000000000000"),
      AbiBool.of(false));

    Assert.assertEquals(
      AbiBool.Type.get().decode("0000000000000000000000000000000000000000000000000000000000000001"),
      AbiBool.of(true));

    Assert.assertEquals(
      AbiBool.Type.get().decode(
        "0000000000000000000000000000000000000000000000007fffffffffffffff0000000000000000000000000000000000000000000000"
          + "0000000000000000000000000000000000000000000000000000000000000000007fffffffffffffff",
        64),
      AbiBool.of(false));

    Assert.assertEquals(
      AbiBool.Type.get().decode(
        "0000000000000000000000000000000000000000000000007fffffffffffffff0000000000000000000000000000000000000000000000"
          + "0000000000000000010000000000000000000000000000000000000000000000007fffffffffffffff",
        64),
      AbiBool.of(true));
  }

  @Test
  public void testEncode() {
    Assert.assertEquals("0000000000000000000000000000000000000000000000000000000000000000", AbiBool.of(false).encode());
    Assert.assertEquals("0000000000000000000000000000000000000000000000000000000000000001", AbiBool.of(true).encode());
  }

  @Test
  public void testType() {
    Assert.assertEquals(AbiBool.Type.get().name, "bool");
  }

}
