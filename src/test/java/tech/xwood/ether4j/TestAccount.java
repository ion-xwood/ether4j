package tech.xwood.ether4j;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAccount {

  @Test
  public void testGenerate() {

    final Account account = Account.generate();
    Assert.assertNotEquals(account.getPrivateKey(), account.getPublicKey());
    Assert.assertEquals(account, Account.of(account.getPrivateKey()));
    Assert.assertNotEquals(Account.generate(), Account.generate());
  }

  @Test
  public void testOf() {

    final Account account = Account.generate();
    Assert.assertEquals(account, Account.of(account.getPrivateKey()));
  }

}
