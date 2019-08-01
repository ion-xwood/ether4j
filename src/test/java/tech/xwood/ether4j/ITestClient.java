package tech.xwood.ether4j;

import java.io.File;
import java.math.BigInteger;
import org.testng.Assert;
import org.testng.annotations.Test;
import tech.xwood.ether4j.abi.AbiAddress;
import tech.xwood.ether4j.abi.AbiArrayDynamic;
import tech.xwood.ether4j.abi.AbiUint;
import tech.xwood.ether4j.abi.AbiValue;

public class ITestClient {

  private static TransactionReceipt deployContract(final Client client, final Account account) {

    final AbiValue[] contractConstrucorArgs = new AbiValue[] {
      AbiArrayDynamic.of(AbiAddress.of(account.getAddress())),
      AbiUint.of(256, System.currentTimeMillis() / 1000 + 2) //sec expire
    };
    final File contractFile = new File("src/sol/Ballot.sol");
    final String contractName = "Ballot";
    final Quantity gasLimit = Quantity.of(1000_000);
    final int optimizationLevel = 8;
    final Quantity txHash = client.deployContract(
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

  private final Client client = new Client("http://127.0.0.1:8545/rpc");

  @Test
  public void testDeployContract() {

    final Account account = TestUtils.createTestAccount(client, Quantity.of(0.5, Unit.ETHER));
    final TransactionReceipt receipt = deployContract(client, account);
    Assert.assertNotNull(receipt.getContractAddress());
  }

  @Test
  public void testEstimateGas() throws Exception {

    final Account account = TestUtils.createTestAccount(client, Quantity.of(0.5, Unit.ETHER));
    final TransactionReceipt receipt = deployContract(client, account);
    final Quantity contractAddr = receipt.getContractAddress();

    Assert.assertNotNull(receipt.getContractAddress());

    final Quantity expectGas = Quantity.of(100_000);
    final Quantity gasEstimate = client.estimateGas(
      account.getAddress(),
      contractAddr,
      expectGas,
      client.getGasPrice(),
      null,
      "vote",
      new AbiValue[] { AbiAddress.of(account.getAddress()) });

    Assert.assertTrue(gasEstimate.toBigInteger().compareTo(BigInteger.ZERO) > 0);
    Assert.assertTrue(expectGas.toBigInteger().compareTo(gasEstimate.toBigInteger()) >= 0);
  }

  @Test
  public void testGetGasPrice() {
    Assert.assertTrue(client.getGasPrice().toHex().startsWith("0x"));
  }

  @Test
  public void testTransferRaw() throws Exception {

    final Quantity coinBase = client.getCoinbase();
    final Account accountA = Account.generate();
    final Account accountB = Account.generate();

    Assert.assertEquals(client.getBalance(accountA.getAddress()), Quantity.of(0));
    Assert.assertEquals(client.getBalance(accountB.getAddress()), Quantity.of(0));

    //coinBase -[1]-> A
    {
      final Quantity sendAmount = Quantity.of(2, Unit.ETHER);
      final Quantity txHash = client.sendTransaction(Transaction.create()
        .setFrom(coinBase)
        .setTo(accountA.getAddress())
        .setValue(sendAmount));
      TestUtils.waitUntilTxPending(client, txHash);
      Assert.assertEquals(client.getBalance(accountA.getAddress()), sendAmount);
      Assert.assertEquals(client.getBalance(accountB.getAddress()), Quantity.of(0));
    }

    //A -[0.5]-> B
    {
      final Quantity sendAmount = Quantity.of(1, Unit.ETHER);
      final Quantity txHash = client.sendRawTransaction(
        accountA,
        Transaction.create()
          .setGasPrice(client.getGasPrice())
          .setGasLimit(Quantity.of(100_000))
          .setTo(accountB.getAddress())
          .setNonce(client.getNonce(accountA.getAddress()))
          .setValue(sendAmount));
      TestUtils.waitUntilTxPending(client, txHash);

      final double balanceA = client.getBalance(accountA.getAddress()).toDecimal(Unit.ETHER).doubleValue();
      Assert.assertTrue(balanceA <= 1D && balanceA >= 0D);
      Assert.assertEquals(client.getBalance(accountB.getAddress()), sendAmount);
    }

  }

}
