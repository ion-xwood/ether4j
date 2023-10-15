package tech.xwood.ether4j.domain;

import org.testng.Assert;
import org.testng.annotations.Test;
import tech.xwood.ether4j.json.JsonCodec;

public class TransactionTest {

  @Test
  public void testToFromJson() {
    final Transaction tx = Transaction
      .create()
      .setBlockHash(QuantityTest.randomQuanity())
      .setBlockNumber(QuantityTest.randomQuanity())
      .setData(QuantityTest.randomQuanity())
      .setFrom(QuantityTest.randomQuanity())
      .setGasLimit(QuantityTest.randomQuanity())
      .setGasPrice(QuantityTest.randomQuanity())
      .setHash(QuantityTest.randomQuanity())
      .setInput(QuantityTest.randomQuanity())
      .setNonce(QuantityTest.randomQuanity())
      .setR(QuantityTest.randomQuanity())
      .setS(QuantityTest.randomQuanity())
      .setTo(QuantityTest.randomQuanity())
      .setTransactionIndex(QuantityTest.randomQuanity())
      .setV(QuantityTest.randomQuanity())
      .setValue(QuantityTest.randomQuanity());
    Assert.assertEquals(
      tx,
      JsonCodec.fromJson(JsonCodec.toJson(tx), Transaction.class));
  }

}
