package tech.xwood.ether4j.client;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.xwood.ether4j.abi.AbiFunction;
import tech.xwood.ether4j.abi.AbiType;
import tech.xwood.ether4j.abi.AbiValue;
import tech.xwood.ether4j.domain.Account;
import tech.xwood.ether4j.domain.Crypto;
import tech.xwood.ether4j.domain.Error;
import tech.xwood.ether4j.domain.Quantity;
import tech.xwood.ether4j.domain.Tag;
import tech.xwood.ether4j.domain.Transaction;
import tech.xwood.ether4j.domain.TransactionReceipt;
import tech.xwood.ether4j.json.JsonCodec;
import tech.xwood.ether4j.solidity.SolidityCompiler;

/**
 * @see https://github.com/ethereum/wiki/wiki/JSON-RPC
 */
public class EtherClient implements AutoCloseable {

  private final SolidityCompiler solidityCompiller;

  private final String uri;

  private final CloseableHttpClient httpClient;

  public EtherClient(final String uri) {
    this.solidityCompiller = SolidityCompiler.create();
    this.uri = uri;
    this.httpClient = HttpClients.createDefault();
    //typeMapStringString = Json.MAPPER.getTypeFactory().constructMapLikeType(HashMap.class, String.class, String.class);
  }

  /**
   * Executes a new message call immediately without creating a transaction on the block chain.
   *
   * @param from - (optional) The address the transaction is sent from.
   * @param to - The address the transaction is directed to.
   * @param gasLimit - (optional) Integer of the gas provided for the transaction execution. eth_call consumes zero gas, but this parameter may be needed by some executions.
   * @param gasPrice - (optional) Integer of the gasPrice used for each paid gas
   * @param value - (optional) Integer of the value sent with this transaction
   * @param data - (optional) Hash of the method signature and encoded parameters. For details see Ethereum Contract ABI
   * @param tag - integer block number, or the string "latest", "earliest" or "pending", see the default block parameter
   * @param functionName
   * @param args
   * @param resultTypes
   * @return the return value of executed contract.
   */
  public AbiValue[] callContractMethodLocal(
    final Quantity from,
    final Quantity to,
    final Quantity gasLimit,
    final Quantity gasPrice,
    final Quantity value,
    final Tag tag,
    final String functionName,
    final AbiValue[] args,
    final AbiType[] resultTypes) {
    final Transaction tx = Transaction.create()
      .setFrom(from)
      .setTo(to)
      .setGasLimit(gasLimit)
      .setGasPrice(gasPrice)
      .setValue(value)
      .setData(AbiFunction.encodeCallAsQuantity(functionName, args));
    final String result = this.rpcRequest(String.class, "eth_call", tx, tag.getName());
    return AbiFunction.decodeResult(result.substring(2), resultTypes);
  }

  /**
   * Executes a new message call immediately without creating a transaction on the block chain.
   *
   * @param from - Account address the transaction is sent from.
   * @param to - The address the transaction is directed to.
   * @param gasLimit - (optional) Integer of the gas provided for the transaction execution. eth_call consumes zero gas, but this parameter may be needed by some executions.
   * @param gasPrice - (optional) Integer of the gasPrice used for each paid gas
   * @param value - (optional) Integer of the value sent with this transaction
   * @param data - (optional) Hash of the method signature and encoded parameters. For details see Ethereum Contract ABI
   * @return transaction hash.
   */
  public Quantity callContractMethodTransact(
    final Account from,
    final Quantity to,
    final Quantity gasLimit,
    final Quantity gasPrice,
    final Quantity value,
    final String functionName,
    final AbiValue[] args) {
    final Transaction tx = Transaction.create()
      .setTo(to)
      .setGasLimit(gasLimit)
      .setGasPrice(gasPrice)
      .setValue(value)
      .setNonce(this.getNonce(from.address()))
      .setData(AbiFunction.encodeCallAsQuantity(functionName, args));
    return this.sendRawTransaction(from, tx);
  }

  @Override
  public void close() {
    try {
      this.httpClient.close();
    }
    catch (final IOException e) {
    }
  }

  /**
   * @return Transaction hash
   */
  public Quantity deployContract(
    final File solFile,
    final String contractName,
    final AbiValue[] contractConstrucorArgs,
    final Account fromAccount,
    final Quantity gasLimit,
    final Integer optimizerLevel) {
    final Quantity deployByteCode;
    {
      final var compilerTask = new SolidityCompiler.Task()
        .addSource(solFile)
        .setOptimizer(optimizerLevel);
      final var output = this.solidityCompiller.compile(compilerTask);
      final var byteCode = output.getByteCode(solFile.getName(), contractName);
      final var constructorCall = AbiFunction.encodeConstructorCall(contractConstrucorArgs);
      deployByteCode = byteCode.appendHex(constructorCall);
    }
    final Transaction transaction;
    {
      transaction = Transaction.create()
        .setGasLimit(gasLimit)
        .setGasPrice(this.getGasPrice())
        .setNonce(this.getNonce(fromAccount.address()))
        .setData(deployByteCode);
    }
    return this.sendRawTransaction(fromAccount, transaction);
  }

  /**
   * Generates and returns an estimate of how much gas is necessary to allow the transaction to complete.
   * The transaction will not be added to the blockchain.
   * Note that the estimate may be significantly more than the amount of gas actually used by the transaction,
   * for a variety of reasons including EVM mechanics and node performance.
   *
   * @param from - (optional) The address the transaction is sent from.
   * @param to - The address the transaction is directed to.
   * @param gasLimit - (optional) Integer of the gas provided for the transaction execution. eth_call consumes zero gas, but this parameter may be needed by some executions.
   * @param gasPrice - (optional) Integer of the gasPrice used for each paid gas
   * @param value - (optional) Integer of the value sent with this transaction
   * @param data - (optional) Hash of the method signature and encoded parameters. For details see Ethereum Contract ABI
   * @param functionName
   * @param args
   * @return
   */
  public Quantity estimateGas(
    final Quantity from,
    final Quantity to,
    final Quantity gasLimit,
    final Quantity gasPrice,
    final Quantity value,
    final String functionName,
    final AbiValue[] args) {
    final Transaction tx = Transaction.create()
      .setFrom(from)
      .setTo(to)
      .setGasLimit(gasLimit)
      .setGasPrice(gasPrice)
      .setValue(value)
      .setData(AbiFunction.encodeCallAsQuantity(functionName, args));
    return this.rpcRequest(Quantity.class, "eth_estimateGas", tx);
  }

  /**
   * @return Returns a list of addresses owned by client.
   */
  public Set<Quantity> getAccounts() {
    final ArrayNode response = this.rpcRequest(ArrayNode.class, "eth_accounts");
    final Set<Quantity> accounts = new LinkedHashSet<>();
    for (int i = 0; i < response.size(); i++) {
      accounts.add(Quantity.of(response.get(i).asText()));
    }
    return accounts;
  }

  /**
  *
  * @param address - 20 Bytes - address to check for balance.
  * @return Returns the balance of the account of given address.
  */
  public Quantity getBalance(final Quantity address) {
    return this.getBalance(address, Tag.LATEST);
  }

  /**
   *
   * @param address - 20 Bytes - address to check for balance.
   * @param blockNumber - integer block number, or the string tag
   * @return Returns the balance of the account of given address.
   */
  public Quantity getBalance(final Quantity address, final Quantity blockNumber) {
    return this.rpcRequest(Quantity.class, "eth_getBalance", address.toHexAddress(), blockNumber.toHex());
  }

  /**
  *
  * @param address - 20 Bytes - address to check for balance.
  * @param tag
  * @return Returns the balance of the account of given address.
  */
  public Quantity getBalance(final Quantity address, final Tag tag) {
    return this.rpcRequest(Quantity.class, "eth_getBalance", address.toHexAddress(), tag.getName());
  }

  /**
   * @param blockHash - hash of a block.
   * @return Returns the number of transactions in a block from a block matching the given block hash.
   */
  public Quantity getBlockTransactionCountByHash(final Quantity blockHash) {
    return this.rpcRequest(Quantity.class, "eth_getBlockTransactionCountByHash", blockHash.toHexHash());
  }

  /**
  * @param blockNumber - block number
  * @return Returns the number of transactions in a block matching the given block number.
  */
  public Quantity getBlockTransactionCountByNumber(final Quantity blockNumber) {
    return this.rpcRequest(Quantity.class, "eth_getBlockTransactionCountByNumber", blockNumber.toHex());
  }

  /**
  * @param quantityOrTag - integer block number
  * @return Returns the number of transactions in a block matching the given block number.
  */
  public Quantity getBlockTransactionCountByNumber(final Tag blockTag) {
    return this.rpcRequest(Quantity.class, "eth_getBlockTransactionCountByNumber", blockTag.getName());
  }

  /**
   * @return Returns the client coinbase address.
   */
  public Quantity getCoinbase() {
    return this.rpcRequest(Quantity.class, "eth_coinbase");
  }

  /**
   * @return Returns the current price per gas in wei.
   */
  public Quantity getGasPrice() {
    return this.rpcRequest(Quantity.class, "eth_gasPrice");
  }

  /**
   * @return Returns the number of hashes per second that the node is mining with.
   */
  public Quantity getHashRate() {
    return this.rpcRequest(Quantity.class, "eth_hashrate");
  }

  /**
   * @return Returns the number of most recent block.
   */
  public Quantity getLastBlockNumber() {
    return this.rpcRequest(Quantity.class, "eth_blockNumber");
  }

  /**
   * @return Returns the current network id.
   */
  public Quantity getNetVersion() {
    return Quantity.of(Integer.parseInt(this.rpcRequest(String.class, "net_version")));
  }

  /**
  * @param address - 20 Bytes - address.
  * @return Returns the number of transactions sent from an address.
  */
  public Quantity getNonce(final Quantity address) {
    return this.getTransactionCount(address, Tag.LATEST);
  }

  /**
   * @return Returns number of peers currently connected to the client.
   */
  public Quantity getPeerCount() {
    return this.rpcRequest(Quantity.class, "net_peerCount");
  }

  /**
   * @return Returns the current ethereum protocol version.
   */
  public Quantity getProtocolVersion() {
    return this.rpcRequest(Quantity.class, "eth_protocolVersion");
  }

  public SolidityCompiler getSolidityCompiller() {
    return this.solidityCompiller;
  }

  /**
  *
  * @param address - 20 Bytes - address of the storage.
  * @param position - integer of the position in the storage.
  * @param blockNumber - integer block number
  * @return Returns the value from a storage position at a given address.
  */
  public Quantity getStorageAt(final Quantity address, final Quantity position, final Quantity blockNumber) {
    return this.rpcRequest(Quantity.class, "eth_getStorageAt", address.toHexAddress(), position.toHex(), blockNumber.toHex());
  }

  /**
  *
  * @param address - 20 Bytes - address of the storage.
  * @param position - integer of the position in the storage.
  * @param blockTag
  * @return Returns the value from a storage position at a given address.
  */
  public Quantity getStorageAt(final Quantity address, final Quantity position, final Tag blockTag) {
    return this.rpcRequest(Quantity.class, "eth_getStorageAt", address.toHexAddress(), position.toHex(), blockTag.getName());
  }

  /**
   * @param hash - 32 Bytes - hash of a transaction
   * @return Returns the information about a transaction requested by transaction hash.
   */
  public Transaction getTransactionByHash(final Quantity hash) {
    return this.rpcRequest(Transaction.class, "eth_getTransactionByHash", hash.toHexHash());
  }

  /**
  * @param address - 20 Bytes - address.
  * @param blockNumber - integer block number
  * @return Returns the number of transactions sent from an address.
  */
  public Quantity getTransactionCount(final Quantity address, final Quantity blockNumber) {
    return this.rpcRequest(Quantity.class, "eth_getTransactionCount", address.toHexAddress(), blockNumber.toHex());
  }

  /**
  * @param address - 20 Bytes - address.
  * @param blockTag
  * @return Returns the number of transactions sent from an address.
  */
  public Quantity getTransactionCount(final Quantity address, final Tag blockTag) {
    return this.rpcRequest(Quantity.class, "eth_getTransactionCount", address.toHexAddress(), blockTag.getName());
  }

  /**
   * @param txHash - 32 Bytes - hash of a transaction
   * @return Returns the receipt of a transaction by transaction hash.
   *        Note That the receipt is not available for pending transactions.
   */
  public TransactionReceipt getTransactionReceipt(final Quantity txHash) {
    return this.rpcRequest(TransactionReceipt.class, "eth_getTransactionReceipt", txHash.toHexHash());
  }

  /**
   * @return Returns true if client is actively listening for network connections.
   */
  public boolean isListening() {
    return this.rpcRequest(boolean.class, "net_listening");
  }

  /**
   * @return Returns true if client is actively mining new blocks.
   */
  public boolean isMining() {
    return this.rpcRequest(Boolean.class, "eth_mining");
  }

  /**
   * Generates a new private key and stores it in the key store directory. The key file is encrypted with the given passphrase.
   * @param passphrase Passphrase
   * @return Returns the address of the new account.
   */
  public Quantity newPersonalAccount(final String passphrase) {
    return this.rpcRequest(Quantity.class, "personal_newAccount", passphrase);
  }

  private <T> T rpcRequest(final Class<T> resultType, final String method, final Object... params) {
    final String rpcRequestId = UUID.randomUUID().toString();
    final Map<String, Object> jsonRpcRequest = new HashMap<>(5);
    jsonRpcRequest.put("id", rpcRequestId);
    jsonRpcRequest.put("jsonrpc", "2.0");
    jsonRpcRequest.put("method", method);
    if (params != null && params.length > 0) {
      jsonRpcRequest.put("params", params);
    }
    final byte[] jsonRpcRequestBytes = JsonCodec.toJsonAsBytes(jsonRpcRequest, false);
    final HttpPost post = new HttpPost(this.uri);
    post.setHeader("Content-Type", "application/json");
    post.setEntity(new ByteArrayEntity(jsonRpcRequestBytes));
    try (CloseableHttpResponse response = this.httpClient.execute(post)) {
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new Error(this.uri + " " + method + " " + response.getStatusLine() + "\n\n" + EntityUtils.toString(response.getEntity()));
      }
      final ObjectNode jsonRpcResponse = JsonCodec.fromJson(response.getEntity().getContent(), ObjectNode.class);
      final String rpcResponseId = jsonRpcResponse.get("id") == null ? "" : jsonRpcResponse.get("id").asText();
      if (!rpcRequestId.equals(rpcResponseId)) {
        throw new Error("Request id and response id are not equals.");
      }
      if (jsonRpcResponse.has("error")) {
        final var rpcError = jsonRpcResponse.withObject("/error");
        throw new Error(
          rpcError.get("code").asInt(),
          rpcError.toPrettyString());
      }
      return JsonCodec.fromJson(jsonRpcResponse.get("result"), resultType);
    }
    catch (final IOException e) {
      throw new Error(e);
    }
  }

  /**
   * Creates new message call transaction or a contract creation for signed transactions.
   * @return - 32 Bytes - the transaction hash, or the zero hash if the transaction is not yet available.
   *        Use eth_getTransactionReceipt to get the contract address, after the transaction was mined, when you created a contract.
   */
  public Quantity sendRawTransaction(final Account account, final Transaction transaction) {
    final var rawTx = Crypto.createRawTransaction(account.privateKey(), account.publicKey(), transaction);
    return this.rpcRequest(Quantity.class, "eth_sendRawTransaction", rawTx.toHex());
  }

  /**
   * Creates new message call transaction or a contract creation, if the data field contains code.
   * @return -  32 Bytes - the transaction hash, or the zero hash if the transaction is not yet available.
   *        Use eth_getTransactionReceipt to get the contract address, after the transaction was mined, when you created a contract.
   */
  public Quantity sendTransaction(final Transaction transaction) {
    return this.rpcRequest(Quantity.class, "eth_sendTransaction", transaction);
  }

  public boolean startMiner() {
    return this.rpcRequest(boolean.class, "miner_start"); //, threadCount
  }

  public boolean stopMiner() {
    return this.rpcRequest(boolean.class, "miner_stop");
  }

  /**
   * Decrypts the key with the given address from the key store.
   * The account can be used with eth_sign and eth_sendTransaction while it is unlocked.
   *
   * If you want to type in the passphrase and stil override the default unlock duration, pass null as the passphrase.
   *
   * @param address Address
   * @param passphrase Passphrase
   * @param duration Seconds
   * @return Success
   */
  public boolean unlockPersonalAccount(final Quantity address, final String passphrase, final int duration) {
    return this.rpcRequest(boolean.class, "personal_unlockAccount", address.toHexAddress(), passphrase, duration);
  }

}
