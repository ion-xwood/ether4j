package tech.xwood.ether4j;

import java.util.concurrent.TimeUnit;
import org.testng.Assert;

public class TestUtils {

  public static Account createTestAccount(final Client client, final Quantity balance) {

    final Account account = Account.generate();
    final Quantity coinBase = client.getCoinbase();
    final Quantity txHash = client.sendTransaction(
      Transaction.create()
        .setFrom(coinBase)
        .setTo(account.getAddress())
        .setNonce(client.getNonce(coinBase))
        .setValue(balance));

    TestUtils.waitUntilTxPending(client, txHash);
    Assert.assertEquals(client.getBalance(account.getAddress(), Tag.LATEST), balance);
    return account;
  }

  public static Transaction waitUntilTxPending(final Client client, final Quantity txHash) {
    return waitUntilTxPending(client, txHash, 100, 10);
  }

  public static Transaction waitUntilTxPending(
    final Client client,
    final Quantity txHash,
    final long delayMs,
    final int limit) {

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
