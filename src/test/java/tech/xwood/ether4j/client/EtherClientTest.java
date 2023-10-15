package tech.xwood.ether4j.client;

import java.io.File;
import java.math.BigInteger;
import org.testng.Assert;
import org.testng.annotations.Test;
import tech.xwood.ether4j.abi.AbiAddress;
import tech.xwood.ether4j.abi.AbiArrayDynamic;
import tech.xwood.ether4j.abi.AbiUint;
import tech.xwood.ether4j.abi.AbiValue;
import tech.xwood.ether4j.domain.Account;
import tech.xwood.ether4j.domain.Quantity;
import tech.xwood.ether4j.domain.Transaction;
import tech.xwood.ether4j.domain.TransactionReceipt;
import tech.xwood.ether4j.domain.Unit;

public class EtherClientTest extends AbstractGethTest {

  private static TransactionReceipt deployContract(final EtherClient client, final Account account) {
    //
    final var contractConstrucorArgs = new AbiValue[] {
      AbiArrayDynamic.of(AbiAddress.of(account.address())),
      AbiUint.of(256, System.currentTimeMillis() / 1000 + 2) //sec expire
    };
    final var contractFile = new File("src/test/resources/sol/Ballot.sol");
    final var contractName = "Ballot";
    final var gasLimit = Quantity.of(1_000_000);
    final var optimizationLevel = 8;
    final var txHash = client.deployContract(
      contractFile,
      contractName,
      contractConstrucorArgs,
      account,
      gasLimit,
      optimizationLevel);
    TestUtils.waitUntilTxPending(client, txHash);
    final TransactionReceipt receipt = client.getTransactionReceipt(txHash);
    return receipt;
  }
  //private final Client client = new Client("http://127.0.0.1:8545/rpc");

  @Test
  public void testDeployContract() {
    try (var client = new EtherClient(getGethURI())) {
      final var account = TestUtils.createTestAccount(client, Quantity.of(0.5, Unit.ETHER));
      final var receipt = deployContract(client, account);
      Assert.assertNotNull(receipt.getContractAddress());
    }
  }

  @Test
  public void testEstimateGas() throws Exception {
    try (var client = new EtherClient(getGethURI())) {
      final var account = TestUtils.createTestAccount(client, Quantity.of(0.5, Unit.ETHER));
      final var receipt = deployContract(client, account);
      final var contractAddr = receipt.getContractAddress();
      Assert.assertNotNull(receipt.getContractAddress());
      final var expectGas = Quantity.of(500_000);
      final var gasEstimate = client.estimateGas(
        account.address(),
        contractAddr,
        expectGas,
        client.getGasPrice(),
        null,
        "vote",
        new AbiValue[] { AbiAddress.of(account.address()) });
      Assert.assertTrue(gasEstimate.toBigInteger().compareTo(BigInteger.ZERO) > 0);
      Assert.assertTrue(expectGas.toBigInteger().compareTo(gasEstimate.toBigInteger()) >= 0);
    }
  }

  @Test
  public void testGetGasPrice() {
    try (var client = new EtherClient(getGethURI())) {
      Assert.assertTrue(client.getGasPrice().toHex().startsWith("0x"));
    }
  }

  @Test
  public void testTransferRaw() throws Exception {
    try (var client = new EtherClient(getGethURI())) {
      final var coinBase = client.getCoinbase();
      final var accountA = Account.generate();
      final var accountB = Account.generate();
      Assert.assertEquals(client.getBalance(accountA.address()), Quantity.of(0));
      Assert.assertEquals(client.getBalance(accountB.address()), Quantity.of(0));
      //coinBase -[1]-> A
      {
        final var sendAmount = Quantity.of(2, Unit.ETHER);
        final var txHash = client.sendTransaction(Transaction.create()
          .setFrom(coinBase)
          .setTo(accountA.address())
          .setValue(sendAmount));
        TestUtils.waitUntilTxPending(client, txHash);
        Assert.assertEquals(client.getBalance(accountA.address()), sendAmount);
        Assert.assertEquals(client.getBalance(accountB.address()), Quantity.of(0));
      }
      //A -[0.5]-> B
      {
        final var sendAmount = Quantity.of(1, Unit.ETHER);
        final var txHash = client.sendRawTransaction(
          accountA,
          Transaction.create()
            .setGasPrice(client.getGasPrice())
            .setGasLimit(Quantity.of(500_000))
            .setTo(accountB.address())
            .setNonce(client.getNonce(accountA.address()))
            .setValue(sendAmount));
        TestUtils.waitUntilTxPending(client, txHash);
        final var balanceA = client.getBalance(accountA.address()).toDecimal(Unit.ETHER).doubleValue();
        Assert.assertTrue(balanceA <= 1D && balanceA >= 0D);
        Assert.assertEquals(client.getBalance(accountB.address()), sendAmount);
      }
    }
  }

}
