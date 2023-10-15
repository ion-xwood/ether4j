package tech.xwood.ether4j.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.xwood.ether4j.json.JsonCodec;

public class TransactionReceipt {

  @JsonCreator
  public static TransactionReceipt create() {
    return new TransactionReceipt();
  }

  /**
   * 32 Bytes - hash of the transaction.
   */
  @JsonProperty("transactionHash")
  private Quantity transactionHash;

  /**
   * Integer of the transactions index position in the block.
   */
  @JsonProperty("transactionIndex")
  private Quantity transactionIndex;

  /**
   * 32 Bytes - hash of the block where this transaction was in.
   */
  @JsonProperty("blockHash")
  private Quantity blockHash;

  /**
   * block number where this transaction was in.
   */
  @JsonProperty("blockNumber")
  private Quantity blockNumber;

  /**
   * 20 Bytes - address of the sender.
   */
  @JsonProperty("from")
  private Quantity from;

  /**
   * 20 Bytes - address of the receiver. null when its a contract creation transaction.
   */
  @JsonProperty("to")
  private Quantity to;

  /**
   * The total amount of gas used when this transaction was executed in the block.
   */
  @JsonProperty("cumulativeGasUsed")
  private Quantity cumulativeGasUsed;

  /**
   * The amount of gas used by this specific transaction alone. contractAddress: DATA, 20 Bytes - The contract address created, if the transaction was a contract creation, otherwise null.
   */
  @JsonProperty("gasUsed")
  private Quantity gasUsed;

  /**
   * DATA, 20 Bytes - The contract address created, if the transaction was a contract creation, otherwise null.
   */
  @JsonProperty("contractAddress")
  private Quantity contractAddress;

  /**
   * Array of log objects, which this transaction generated.
   */
  @JsonProperty("logs")
  private Event[] logs;

  /**
   *  256 Bytes - Bloom filter for light clients to quickly retrieve related logs.
   */
  @JsonProperty("logsBloom")
  private Quantity logsBloom;

  /**
   * DATA 32 bytes of post-transaction stateroot (pre Byzantium)
   */
  @JsonProperty("root")
  private Quantity root;

  /**
   * QUANTITY either 1 (success) or 0 (failure)
   */
  @JsonProperty("status")
  private Quantity status;

  private TransactionReceipt() {
  }

  public Quantity getBlockHash() {
    return blockHash;
  }

  public Quantity getBlockNumber() {
    return blockNumber;
  }

  public Quantity getContractAddress() {
    return contractAddress;
  }

  public Quantity getCumulativeGasUsed() {
    return cumulativeGasUsed;
  }

  public Quantity getFrom() {
    return from;
  }

  public Quantity getGasUsed() {
    return gasUsed;
  }

  public Event[] getLogs() {
    return logs;
  }

  public Quantity getLogsBloom() {
    return logsBloom;
  }

  public Quantity getRoot() {
    return root;
  }

  public Quantity getStatus() {
    return status;
  }

  public Quantity getTo() {
    return to;
  }

  public Quantity getTransactionHash() {
    return transactionHash;
  }

  public Quantity getTransactionIndex() {
    return transactionIndex;
  }

  public boolean isStatusTrue() {
    return Quantity.of(1).equals(status);
  }

  @Override
  public String toString() {
    return JsonCodec.toJson(this, true);
  }

}
