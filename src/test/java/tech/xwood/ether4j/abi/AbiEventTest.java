package tech.xwood.ether4j.abi;

import org.testng.Assert;
import org.testng.annotations.Test;
import tech.xwood.ether4j.domain.Quantity;

public class AbiEventTest {

  @Test
  public void testDecode() {
    final Quantity address = Quantity.of("0xb7e904467e9725bce6559c0c46a898b4490e5429");
    final Quantity data = Quantity
      .of("0x0000000000000000000000000000000000000000000000000000000000018894000000000000000000000000000000000000000000"
        + "0000000000000000000040000000000000000000000000000000000000000000000000000000000000000f437573746f6d206c6f6720"
        + "746578740000000000000000000000000000000000");
    final Quantity[] topics = {
      Quantity.of("0x4410ee520d4178ae06472c6a5c5730be257067e529361c8f62e14705f5850bc1"),
      Quantity.of("0x000000000000000000000000b7e904467e9725bce6559c0c46a898b4490e5429"),
      Quantity.of("0x000000000000000000000000b7e904467e9725bce6559c0c46a898b4490e5429") };
    final AbiValue[] result = AbiEvent.decode(data, topics, "VoteEvent", AbiEvent.Types.create()
      .indexed(AbiAddress.Type.get())
      .indexed(AbiAddress.Type.get())
      .add(AbiUint.Type.of(256))
      .add(AbiString.Type.get()));
    Assert.assertEquals(((AbiAddress) result[0]).toQuantity(), address);
    Assert.assertEquals(((AbiAddress) result[1]).toQuantity(), address);
    Assert.assertEquals(((AbiUint) result[2]).value.intValue(), 100500);
    Assert.assertEquals(((AbiString) result[3]).value, "Custom log text");
  }

  @Test
  public void testDecodeIndexedValue() {
    final AbiUint v = AbiUint.of(256, 10);
    Assert.assertEquals(v, AbiEvent.decodeIndexedValue(v.encode(), v.type));
  }

  @Test
  public void testGetSignature() {
    Assert.assertEquals(
      "71e71a8458267085d5ab16980fd5f114d2d37f232479c245d523ce8d23ca40ed",
      AbiEvent.getSignature("Notify", AbiUint.Type.of(256), AbiUint.Type.of(256)));
  }

}
