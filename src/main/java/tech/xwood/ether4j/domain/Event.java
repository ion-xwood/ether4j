package tech.xwood.ether4j.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.xwood.ether4j.abi.AbiEvent;
import tech.xwood.ether4j.abi.AbiEvent.Types;
import tech.xwood.ether4j.json.JsonCodec;
import tech.xwood.ether4j.abi.AbiValue;

public class Event {

  @JsonCreator
  public static Event create() {
    return new Event();
  }

  /**
   *  integer of the log index position in the block. null when its pending log.
   */
  @JsonProperty("logIndex")
  private Quantity logIndex;

  /**
   *  integer of the transactions index position log was created from. null when its pending log.
   */
  @JsonProperty("transactionIndex")
  private Quantity transactionIndex;

  /**
   * 32 Bytes - hash of the transactions this log was created from. null when its pending log.
   */
  @JsonProperty("transactionHash")
  private Quantity transactionHash;

  /**
   * 32 Bytes - hash of the block where this log was in. null when its pending. null when its pending log.
   */
  @JsonProperty("blockHash")
  private Quantity blockHash;

  /**
   * the block number where this log was in. null when its pending. null when its pending log.
   */
  @JsonProperty("blockNumber")
  private Quantity blockNumber;

  /**
   * 20 Bytes - address from which this log originated.
   */
  @JsonProperty("address")
  private Quantity address;

  /**
   * contains the non-indexed arguments of the log.
   */
  @JsonProperty("data")
  private Quantity data;

  /**
   * Array of DATA - Array of 0 to 4 32 Bytes DATA of indexed log arguments.
   * (In solidity: The first topic is the hash of the signature of the event (e.g. Deposit(address,bytes32,uint256)),
   * except you declared the event with the anonymous specifier.)
   */
  @JsonProperty("topics")
  private Quantity[] topics;

  @JsonProperty("type")
  private String type;

  private Event() {
  }

  public AbiValue[] decode(final String eventName, final Types types) {
    return AbiEvent.decode(data, topics, eventName, types);
  }

  public Quantity getAddress() {
    return address;
  }

  public Quantity getBlockHash() {
    return blockHash;
  }

  public Quantity getBlockNumber() {
    return blockNumber;
  }

  public Quantity getData() {
    return data;
  }

  public Quantity getLogIndex() {
    return logIndex;
  }

  public Quantity[] getTopics() {
    return topics;
  }

  public Quantity getTransactionHash() {
    return transactionHash;
  }

  public Quantity getTransactionIndex() {
    return transactionIndex;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return JsonCodec.toJson(this, true);
  }

}
