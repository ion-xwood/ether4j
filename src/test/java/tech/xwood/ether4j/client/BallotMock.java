package tech.xwood.ether4j.client;

import java.io.File;
import java.util.List;
import org.testng.Assert;
import tech.xwood.ether4j.abi.AbiAddress;
import tech.xwood.ether4j.abi.AbiArrayDynamic;
import tech.xwood.ether4j.abi.AbiBool;
import tech.xwood.ether4j.abi.AbiEvent;
import tech.xwood.ether4j.abi.AbiString;
import tech.xwood.ether4j.abi.AbiType;
import tech.xwood.ether4j.abi.AbiUint;
import tech.xwood.ether4j.abi.AbiValue;
import tech.xwood.ether4j.domain.Account;
import tech.xwood.ether4j.domain.Error;
import tech.xwood.ether4j.domain.Quantity;
import tech.xwood.ether4j.domain.Tag;
import tech.xwood.ether4j.domain.TransactionReceipt;
import tech.xwood.ether4j.json.JsonCodec;

class BallotMock {

  public static class Winner {

    public final Quantity address;

    public final int voteAmount;

    public Winner(final Quantity address, final int voteAmount) {
      this.address = address;
      this.voteAmount = voteAmount;
    }

    @Override
    public String toString() {
      return JsonCodec.toJson(this, true);
    }

  }

  public static Quantity deploy(
    final EtherClient client,
    final Account deployAccount,
    final List<Quantity> candidateAddrs,
    final long expirationTime) {
    //
    final var contractConstrucorArgs = new AbiValue[] {
      AbiArrayDynamic.of(
        candidateAddrs.stream()
          .map(AbiAddress::of)
          .toArray(s -> new AbiValue[s])),
      AbiUint.of(256, expirationTime)
    };
    final var contractFile = new File("src/test/resources/sol/Ballot.sol");
    final var contractName = "Ballot";
    final var gasLimit = Quantity.of(1_000_000);
    final var txHash = client.deployContract(
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

  private final EtherClient client;

  private final Quantity contractAddr;

  private final Quantity defaultGasLimit;

  public BallotMock(final EtherClient client, final Quantity contractAddr, final Quantity defaultGasLimit) {
    this.client = client;
    this.contractAddr = contractAddr;
    this.defaultGasLimit = defaultGasLimit;
  }

  public void finish(final Account account) {
    final Quantity txHash = this.client.callContractMethodTransact(
      account,
      this.contractAddr,
      this.defaultGasLimit,
      this.client.getGasPrice(),
      null,
      "finish",
      null);
    TestUtils.waitUntilTxPending(this.client, txHash);
    final TransactionReceipt receipt = this.client.getTransactionReceipt(txHash);
    if (!receipt.isStatusTrue()) {
      throw new Error("Bad status: " + receipt.toString());
    }
  }

  public BallotMock.Winner getWinner(final Quantity fromAddr) {
    final AbiValue[] result = this.client.callContractMethodLocal(
      fromAddr,
      this.contractAddr,
      this.defaultGasLimit,
      this.client.getGasPrice(),
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
    final AbiValue[] result = this.client.callContractMethodLocal(
      fromAddr,
      this.contractAddr,
      this.defaultGasLimit,
      this.client.getGasPrice(),
      null,
      Tag.LATEST,
      "isFinished",
      null,
      new AbiType[] { AbiBool.Type.get() });
    return ((AbiBool) result[0]).value;
  }

  public void vote(final Account account, final Quantity candidateAddr) {
    final Quantity txHash = this.client.callContractMethodTransact(
      account,
      this.contractAddr,
      this.defaultGasLimit,
      this.client.getGasPrice(),
      null,
      "vote",
      new AbiValue[] { AbiAddress.of(candidateAddr) });
    TestUtils.waitUntilTxPending(this.client, txHash);
    final TransactionReceipt receipt = this.client.getTransactionReceipt(txHash);
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
    Assert.assertEquals(((AbiAddress) logValues[0]).toQuantity(), account.address());
    Assert.assertEquals(((AbiAddress) logValues[1]).toQuantity(), account.address());
    Assert.assertEquals(((AbiUint) logValues[2]).value.intValue(), 100500);
    Assert.assertEquals(((AbiString) logValues[3]).value, "Custom log text");
  }

}