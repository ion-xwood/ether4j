package tech.xwood.ether4j;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestTransaction {

  @Test
  public void testToFromJson() {

    final Transaction tx = Transaction
      .create()
      .setBlockHash(TestQuantity.randomQuanity())
      .setBlockNumber(TestQuantity.randomQuanity())
      .setData(TestQuantity.randomQuanity())
      .setFrom(TestQuantity.randomQuanity())
      .setGasLimit(TestQuantity.randomQuanity())
      .setGasPrice(TestQuantity.randomQuanity())
      .setHash(TestQuantity.randomQuanity())
      .setInput(TestQuantity.randomQuanity())
      .setNonce(TestQuantity.randomQuanity())
      .setR(TestQuantity.randomQuanity())
      .setS(TestQuantity.randomQuanity())
      .setTo(TestQuantity.randomQuanity())
      .setTransactionIndex(TestQuantity.randomQuanity())
      .setV(TestQuantity.randomQuanity())
      .setValue(TestQuantity.randomQuanity());

    Assert.assertEquals(
      tx,
      Utils.fromJson(Utils.toJson(tx), Transaction.class));
  }

}
