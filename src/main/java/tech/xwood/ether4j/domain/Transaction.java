package tech.xwood.ether4j.domain;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.xwood.ether4j.json.JsonCodec;

public class Transaction {

  @JsonCreator
  public static Transaction create() {
    return new Transaction();
  }

  /**
   * 32 Bytes - hash of the block where this transaction was in. null when its pending.
   */
  private Quantity blockHash;

  /**
   * block number where this transaction was in. null when its pending.
   */
  private Quantity blockNumber;

  /**
   * 20 Bytes - address of the sender.
   */
  private Quantity from;

  /**
   * gas provided by the sender. GasLimit
   */
  private Quantity gasLimit;

  /**
   * gas price provided by the sender in Wei.
   */
  private Quantity gasPrice;

  /**
   * 32 Bytes - hash of the transaction.
   */
  private Quantity hash;

  /**
   * the data send along with the transaction.
   */
  private Quantity input;

  /**
   * the number of transactions made by the sender prior to this one.
   */
  private Quantity nonce;

  /**
   * 20 Bytes - address of the receiver. null when its a contract creation transaction.
   */
  private Quantity to;

  /**
   * integer of the transactions index position in the block. null when its pending.
   */
  private Quantity transactionIndex;

  /**
   * The compiled code of a contract OR the hash of the invoked method signature and encoded parameters. For details see Ethereum Contract ABI
   */
  private Quantity data;

  /**
   * value transferred in Wei.
   */
  private Quantity value;

  /**
   * ECDSA recovery id
   */
  private Quantity v;

  /**
   * 32 Bytes - ECDSA signature
   */
  private Quantity r;

  /**
   * 32 Bytes - ECDSA signature s
   */
  private Quantity s;

  private Transaction() {
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof final Transaction other)) {
      return false;
    }
    return Objects.equals(this.blockHash, other.blockHash)
      && Objects.equals(this.blockNumber, other.blockNumber)
      && Objects.equals(this.data, other.data)
      && Objects.equals(this.from, other.from)
      && Objects.equals(this.gasLimit, other.gasLimit)
      && Objects.equals(this.gasPrice, other.gasPrice)
      && Objects.equals(this.hash, other.hash)
      && Objects.equals(this.input, other.input)
      && Objects.equals(this.nonce, other.nonce)
      && Objects.equals(this.r, other.r)
      && Objects.equals(this.s, other.s)
      && Objects.equals(this.to, other.to)
      && Objects.equals(this.transactionIndex, other.transactionIndex)
      && Objects.equals(this.v, other.v)
      && Objects.equals(this.value, other.value);
  }

  public Quantity getBlockHash() {
    return this.blockHash;
  }

  @JsonProperty("blockHash")
  public String getBlockHashAsHex() {
    return this.blockHash == null ? null : this.blockHash.toHexHash();
  }

  @JsonProperty("blockNumber")
  public Quantity getBlockNumber() {
    return this.blockNumber;
  }

  @JsonProperty("data")
  public Quantity getData() {
    return this.data;
  }

  public Quantity getFrom() {
    return this.from;
  }

  @JsonProperty("from")
  public String getFromAsHex() {
    return this.from == null ? null : this.from.toHexAddress();
  }

  @JsonProperty("gas")
  public Quantity getGasLimit() {
    return this.gasLimit;
  }

  @JsonProperty("gasPrice")
  public Quantity getGasPrice() {
    return this.gasPrice;
  }

  public Quantity getHash() {
    return this.hash;
  }

  @JsonProperty("hash")
  public String getHashAsHex() {
    return this.hash == null ? null : this.hash.toHexHash();
  }

  @JsonProperty("input")
  public Quantity getInput() {
    return this.input;
  }

  @JsonProperty("nonce")
  public Quantity getNonce() {
    return this.nonce;
  }

  @JsonProperty("r")
  public Quantity getR() {
    return this.r;
  }

  @JsonProperty("s")
  public Quantity getS() {
    return this.s;
  }

  public Quantity getTo() {
    return this.to;
  }

  @JsonProperty("to")
  public String getToAsHex() {
    return this.to == null ? null : this.to.toHexAddress();
  }

  @JsonProperty("transactionIndex")
  public Quantity getTransactionIndex() {
    return this.transactionIndex;
  }

  @JsonProperty("v")
  public Quantity getV() {
    return this.v;
  }

  @JsonProperty("value")
  public Quantity getValue() {
    return this.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      this.blockHash,
      this.blockNumber,
      this.data,
      this.from,
      this.gasLimit,
      this.gasPrice,
      this.hash,
      this.input,
      this.nonce,
      this.r,
      this.s,
      this.to,
      this.transactionIndex,
      this.v,
      this.value);
  }

  public boolean isPending() {
    return this.blockNumber == null;
  }

  @JsonProperty("blockHash")
  public Transaction setBlockHash(final Quantity blockHash) {
    this.blockHash = blockHash;
    return this;
  }

  @JsonProperty("blockNumber")
  public Transaction setBlockNumber(final Quantity blockNumber) {
    this.blockNumber = blockNumber;
    return this;
  }

  @JsonProperty("data")
  public Transaction setData(final Quantity data) {
    this.data = data;
    return this;
  }

  @JsonProperty("from")
  public Transaction setFrom(final Quantity from) {
    this.from = from;
    return this;
  }

  @JsonProperty("gas")
  public Transaction setGasLimit(final Quantity gasLimit) {
    this.gasLimit = gasLimit;
    return this;
  }

  @JsonProperty("gasPrice")
  public Transaction setGasPrice(final Quantity gasPrice) {
    this.gasPrice = gasPrice;
    return this;
  }

  @JsonProperty("hash")
  public Transaction setHash(final Quantity hash) {
    this.hash = hash;
    return this;
  }

  @JsonProperty("input")
  public Transaction setInput(final Quantity input) {
    this.input = input;
    return this;
  }

  @JsonProperty("nonce")
  public Transaction setNonce(final Quantity nonce) {
    this.nonce = nonce;
    return this;
  }

  @JsonProperty("r")
  public Transaction setR(final Quantity r) {
    this.r = r;
    return this;
  }

  @JsonProperty("s")
  public Transaction setS(final Quantity s) {
    this.s = s;
    return this;
  }

  @JsonProperty("to")
  public Transaction setTo(final Quantity to) {
    this.to = to;
    return this;
  }

  @JsonProperty("transactionIndex")
  public Transaction setTransactionIndex(final Quantity transactionIndex) {
    this.transactionIndex = transactionIndex;
    return this;
  }

  @JsonProperty("v")
  public Transaction setV(final Quantity v) {
    this.v = v;
    return this;
  }

  @JsonProperty("value")
  public Transaction setValue(final Quantity value) {
    this.value = value;
    return this;
  }

  @Override
  public String toString() {
    return JsonCodec.toJson(this, true);
  }

}
