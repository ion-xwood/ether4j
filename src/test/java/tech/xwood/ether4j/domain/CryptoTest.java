package tech.xwood.ether4j.domain;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CryptoTest {

  @Test
  public void testCreateAddress() {
    final Quantity privateKey = Quantity.of("0x360c7c7fc4f90f52af8385e52eb1e02cbd8d22d37ecf43d8604a827d94fba230");
    final Quantity publicKey = Quantity.of("0xd916a0e9028c04512890b8bd1c75778a786f5df272dd8bfacff4b96dfaa977480738c6324"
      + "889d6fa2cca4ac9dfe5bbf6e5fcbdae0d1f3cf08ba5b7900e19727f");
    final Quantity address = Quantity.of("0x33dad137746366bbac2be3cf8e187bb99aa9648a");
    Assert.assertEquals(publicKey, Crypto.createPublicKey(privateKey));
    Assert.assertEquals(address, Crypto.createAddress(publicKey));
  }

  @Test
  public void testCreatePublicKey() {
    final Quantity privateKey = Crypto.createPrivateKey();
    final Quantity publicKey = Crypto.createPublicKey(privateKey);
    Assert.assertNotEquals(privateKey, publicKey);
    Assert.assertEquals(publicKey, Crypto.createPublicKey(privateKey));
  }

  @Test
  public void testCreateRawTransaction() {
    final Quantity privateKey = Quantity.of("0x360c7c7fc4f90f52af8385e52eb1e02cbd8d22d37ecf43d8604a827d94fba230");
    final Quantity publicKey = Quantity.of("0xd916a0e9028c04512890b8bd1c75778a786f5df272dd8bfacff4b96dfaa977480"
      + "738c6324889d6fa2cca4ac9dfe5bbf6e5fcbdae0d1f3cf08ba5b7900e19727f");
    final Quantity rawTransaction = Quantity.of("0xf8608002029433dad137746366bbac2be3cf8e187bb99aa9648a83018894"
      + "801ca0e832126d063c6cfb14d266b8ba7e24582bb3566ab9406bf01ff91cab67068967a026391f97960b74f41f61602048398b"
      + "97755c7976775676b51b03c9e4ecf52044");
    final Quantity nonce = Quantity.of(0);
    final Quantity gasPrice = Quantity.of(2);
    final Quantity gasLimit = Quantity.of(2);
    final Quantity to = Crypto.createAddress(publicKey);
    final Quantity value = Quantity.of(100500);
    final Quantity data = null;
    final Transaction tx = Transaction
      .create()
      .setNonce(nonce)
      .setGasPrice(gasPrice)
      .setGasLimit(gasLimit)
      .setTo(to)
      .setValue(value)
      .setData(data);
    final Quantity rawTransactionCalc = Crypto.createRawTransaction(privateKey, publicKey, tx);
    Assert.assertEquals(rawTransaction, rawTransactionCalc);
  }

  @Test
  public void testKeccak256() {
    Assert.assertEquals(
      Quantity.of("0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad"),
      Crypto.keccak256(Quantity.of("0x68656c6c6f20776f726c64")));
  }

}
