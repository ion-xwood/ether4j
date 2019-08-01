package tech.xwood.ether4j;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import tech.xwood.ether4j.abi.AbiAddress;
import tech.xwood.ether4j.abi.AbiArrayDynamic;
import tech.xwood.ether4j.abi.AbiBool;
import tech.xwood.ether4j.abi.AbiEvent;
import tech.xwood.ether4j.abi.AbiString;
import tech.xwood.ether4j.abi.AbiType;
import tech.xwood.ether4j.abi.AbiUint;
import tech.xwood.ether4j.abi.AbiValue;

public class ITestBallot {

  private static class Ballot {

    public static class Winner {

      public final Quantity address;
      public final int voteAmount;

      public Winner(final Quantity address, final int voteAmount) {
        this.address = address;
        this.voteAmount = voteAmount;
      }

      @Override
      public String toString() {
        return Utils.toJson(this, true);
      }
    }

    public static Quantity deploy(
      final Client client,
      final Account deployAccount,
      final List<Quantity> candidateAddrs,
      final long expirationTime) {

      final AbiValue[] contractConstrucorArgs = new AbiValue[] {
        AbiArrayDynamic.of(
          candidateAddrs.stream()
            .map(AbiAddress::of)
            .toArray(s -> new AbiValue[s])),
        AbiUint.of(256, expirationTime)
      };

      final File contractFile = new File("src/sol/Ballot.sol");
      final String contractName = "Ballot";
      final Quantity gasLimit = Quantity.of(1000_000);
      final Quantity txHash = client.deployContract(
        contractFile,
        contractName,
        contractConstrucorArgs,
        deployAccount,
        gasLimit,
        8);

      TestUtils.waitUntilTxPending(client, txHash);

      return client.getTransactionReceipt(txHash)
        .getContractAddress();
    }

    private final Client client;
    private final Quantity contractAddr;
    private final Quantity defaultGasLimit;

    public Ballot(final Client client, final Quantity contractAddr, final Quantity defaultGasLimit) {
      this.client = client;
      this.contractAddr = contractAddr;
      this.defaultGasLimit = defaultGasLimit;
    }

    public void finish(final Account account) {

      final Quantity txHash = client.callContractMethodTransact(
        account,
        contractAddr,
        defaultGasLimit,
        client.getGasPrice(),
        null,
        "finish",
        null);
      TestUtils.waitUntilTxPending(client, txHash);
      final TransactionReceipt receipt = client.getTransactionReceipt(txHash);
      if (!receipt.isStatusTrue()) {
        throw new Error("Bad status: " + receipt.toString());
      }
    }

    public Winner getWinner(final Quantity fromAddr) {

      final AbiValue[] result = client.callContractMethodLocal(
        fromAddr,
        contractAddr,
        defaultGasLimit,
        client.getGasPrice(),
        null,
        Tag.LATEST,
        "getWinner",
        null,
        new AbiType[] { AbiAddress.Type.get(), AbiUint.Type.of(32) });

      return new Winner(
        ((AbiAddress) result[0]).toQuantity(),
        ((AbiUint) result[1]).value.intValue());
    }

    public boolean isFinished(final Quantity fromAddr) {

      final AbiValue[] result = client.callContractMethodLocal(
        fromAddr,
        contractAddr,
        defaultGasLimit,
        client.getGasPrice(),
        null,
        Tag.LATEST,
        "isFinished",
        null,
        new AbiType[] { AbiBool.Type.get() });

      return ((AbiBool) result[0]).value;
    }

    public void vote(final Account account, final Quantity candidateAddr) {

      final Quantity txHash = client.callContractMethodTransact(
        account,
        contractAddr,
        defaultGasLimit,
        client.getGasPrice(),
        null,
        "vote",
        new AbiValue[] { AbiAddress.of(candidateAddr) });

      TestUtils.waitUntilTxPending(client, txHash);

      final TransactionReceipt receipt = client.getTransactionReceipt(txHash);
      if (!receipt.isStatusTrue()) {
        throw new Error("Bad status: " + receipt.toString());
      }

      final AbiValue[] logValues = receipt.getLogs()[0].decode(
        "VoteEvent",
        AbiEvent.Types.create()
          .indexed(AbiAddress.Type.get())
          .indexed(AbiAddress.Type.get())
          .add(AbiUint.Type.of(256))
          .add(AbiString.Type.get()));

      Assert.assertEquals(((AbiAddress) logValues[0]).toQuantity(), account.getAddress());
      Assert.assertEquals(((AbiAddress) logValues[1]).toQuantity(), account.getAddress());
      Assert.assertEquals(((AbiUint) logValues[2]).value.intValue(), 100500);
      Assert.assertEquals(((AbiString) logValues[3]).value, "Custom log text");
    }

  }

  @Test
  public void test() throws Exception {

    try (Client client = new Client(System.getProperty("geth_uri", "http://127.0.0.1:8545/rpc"))) {
      client.startMiner(1);
      final Account accountA = TestUtils.createTestAccount(client, Quantity.of(0.5, Unit.ETHER));
      final Account accountB = TestUtils.createTestAccount(client, Quantity.of(0.5, Unit.ETHER));
      final Account accountC = TestUtils.createTestAccount(client, Quantity.of(0.5, Unit.ETHER));
      final Account accountD = TestUtils.createTestAccount(client, Quantity.of(0.5, Unit.ETHER));

      final long expirationTime = System.currentTimeMillis() / 1000 + 2; //exp 2 sec

      final Quantity ballotAddr = Ballot.deploy(
        client,
        accountA,
        Arrays.asList(
          accountA.getAddress(),
          accountB.getAddress(),
          accountC.getAddress(),
          accountD.getAddress()),
        expirationTime);

      final Ballot ballot = new Ballot(client, ballotAddr, Quantity.of(1_000_000));

      { //view function without balance
        final Quantity balanceBefore = client.getBalance(accountA.getAddress());
        Assert.assertFalse(ballot.isFinished(accountA.getAddress()));
        Assert.assertEquals(balanceBefore, client.getBalance(accountA.getAddress()));
      }

      Assert.assertFalse(ballot.isFinished(accountA.getAddress()));

      { //vote
        ballot.vote(accountA, accountA.getAddress());
      }

      //wait expire
      TimeUnit.SECONDS.sleep(4);

      {// finish
        ballot.finish(accountA);
        Assert.assertTrue(ballot.isFinished(accountA.getAddress()));
        final Ballot.Winner winner = ballot.getWinner(accountA.getAddress());
        Assert.assertEquals(winner.voteAmount, 1);
        Assert.assertEquals(winner.address, accountA.getAddress());
      }

    }

  }

}
