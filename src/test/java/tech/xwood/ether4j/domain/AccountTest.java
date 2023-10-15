package tech.xwood.ether4j.domain;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AccountTest {

  @Test
  public void testGenerate() {
    final Account account = Account.generate();
    Assert.assertNotEquals(account.privateKey(), account.publicKey());
    Assert.assertEquals(account, Account.of(account.privateKey()));
    Assert.assertNotEquals(Account.generate(), Account.generate());
  }

  @Test
  public void testOf() {
    final Account account = Account.generate();
    Assert.assertEquals(account, Account.of(account.privateKey()));
  }

}
