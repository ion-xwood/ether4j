package tech.xwood.ether4j;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

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
    if (obj == null || !(obj instanceof Transaction)) {
      return false;
    }
    final Transaction other = (Transaction) obj;
    return Objects.equals(blockHash, other.blockHash)
      && Objects.equals(blockNumber, other.blockNumber)
      && Objects.equals(data, other.data)
      && Objects.equals(from, other.from)
      && Objects.equals(gasLimit, other.gasLimit)
      && Objects.equals(gasPrice, other.gasPrice)
      && Objects.equals(hash, other.hash)
      && Objects.equals(input, other.input)
      && Objects.equals(nonce, other.nonce)
      && Objects.equals(r, other.r)
      && Objects.equals(s, other.s)
      && Objects.equals(to, other.to)
      && Objects.equals(transactionIndex, other.transactionIndex)
      && Objects.equals(v, other.v)
      && Objects.equals(value, other.value);
  }

  public Quantity getBlockHash() {
    return blockHash;
  }

  @JsonProperty("blockHash")
  public String getBlockHashAsHex() {
    return blockHash == null ? null : blockHash.toHexHash();
  }

  @JsonProperty("blockNumber")
  public Quantity getBlockNumber() {
    return blockNumber;
  }

  @JsonProperty("data")
  public Quantity getData() {
    return data;
  }

  public Quantity getFrom() {
    return from;
  }

  @JsonProperty("from")
  public String getFromAsHex() {
    return from == null ? null : from.toHexAddress();
  }

  @JsonProperty("gas")
  public Quantity getGasLimit() {
    return gasLimit;
  }

  @JsonProperty("gasPrice")
  public Quantity getGasPrice() {
    return gasPrice;
  }

  public Quantity getHash() {
    return hash;
  }

  @JsonProperty("hash")
  public String getHashAsHex() {
    return hash == null ? null : hash.toHexHash();
  }

  @JsonProperty("input")
  public Quantity getInput() {
    return input;
  }

  @JsonProperty("nonce")
  public Quantity getNonce() {
    return nonce;
  }

  @JsonProperty("r")
  public Quantity getR() {
    return r;
  }

  @JsonProperty("s")
  public Quantity getS() {
    return s;
  }

  public Quantity getTo() {
    return to;
  }

  @JsonProperty("to")
  public String getToAsHex() {
    return to == null ? null : to.toHexAddress();
  }

  @JsonProperty("transactionIndex")
  public Quantity getTransactionIndex() {
    return transactionIndex;
  }

  @JsonProperty("v")
  public Quantity getV() {
    return v;
  }

  @JsonProperty("value")
  public Quantity getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      blockHash,
      blockNumber,
      data,
      from,
      gasLimit,
      gasPrice,
      hash,
      input,
      nonce,
      r,
      s,
      to,
      transactionIndex,
      v,
      value);
  }

  public boolean isPending() {
    return blockNumber == null;
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
    return Utils.toJson(this, true);
  }
}
