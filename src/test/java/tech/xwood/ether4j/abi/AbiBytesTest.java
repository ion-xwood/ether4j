package tech.xwood.ether4j.abi;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AbiBytesTest {

  @Test
  public void testDecode() {
    Assert.assertEquals(
      AbiBytes.Type.of(6).decode("0001020304050000000000000000000000000000000000000000000000000000"),
      AbiBytes.Type.of(6).valueOf(new byte[] { 0, 1, 2, 3, 4, 5 }));
    Assert.assertEquals(
      AbiBytes.Type.of(1).decode("0000000000000000000000000000000000000000000000000000000000000000"),
      AbiBytes.Type.of(1).valueOf(new byte[] { 0 }));
    Assert.assertEquals(
      AbiBytes.Type.of(4).decode("6461766500000000000000000000000000000000000000000000000000000000"),
      AbiBytes.of("dave".getBytes()));
  }

  @Test
  public void testEncode() {
    Assert.assertEquals(
      "0001020304050000000000000000000000000000000000000000000000000000",
      AbiBytes.of(new byte[] { 0, 1, 2, 3, 4, 5 }).encode());
    Assert.assertEquals(
      "0000000000000000000000000000000000000000000000000000000000000000",
      AbiBytes.of(new byte[] { 0 }).encode());
    Assert.assertEquals(
      "6461766500000000000000000000000000000000000000000000000000000000",
      AbiBytes.of("dave".getBytes()).encode());
  }

  @Test
  public void testType() {
    Assert.assertEquals(AbiBytes.Type.of(32).name, "bytes32");
    Assert.assertEquals(AbiBytes.Type.of(32), AbiBytes.Type.of(32));
  }

}
