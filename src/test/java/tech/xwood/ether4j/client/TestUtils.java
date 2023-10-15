package tech.xwood.ether4j.client;

import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import tech.xwood.ether4j.domain.Account;
import tech.xwood.ether4j.domain.Error;
import tech.xwood.ether4j.domain.Quantity;
import tech.xwood.ether4j.domain.Tag;
import tech.xwood.ether4j.domain.Transaction;

class TestUtils {

  public static Account createTestAccount(final EtherClient client, final Quantity balance) {
    final var account = Account.generate();
    final var coinBase = client.getCoinbase();
    final var txHash = client.sendTransaction(
      Transaction.create()
        .setFrom(coinBase)
        .setTo(account.address())
        .setNonce(client.getNonce(coinBase))
        .setValue(balance));
    TestUtils.waitUntilTxPending(client, txHash);
    Assert.assertEquals(client.getBalance(account.address(), Tag.LATEST), balance);
    return account;
  }

  public static Transaction waitUntilTxPending(final EtherClient client, final Quantity txHash) {
    return waitUntilTxPending(client, txHash, 100, 10);
  }

  public static Transaction waitUntilTxPending(
    final EtherClient client,
    final Quantity txHash,
    final long delayMs,
    final int limit) {
    //
    for (int i = 0; i < limit; i++) {
      final Transaction tx = client.getTransactionByHash(txHash);
      if (!tx.isPending()) {
        return tx;
      }
      try {
        TimeUnit.MILLISECONDS.sleep(delayMs);
      }
      catch (final InterruptedException e) {
        break;
      }
    }
    throw new Error("Tx wait panding timeout");
  }

}
