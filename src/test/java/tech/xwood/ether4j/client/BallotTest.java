package tech.xwood.ether4j.client;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import tech.xwood.ether4j.domain.Quantity;
import tech.xwood.ether4j.domain.Unit;

class BallotTest extends AbstractGethTest {

  @Test
  public void test() throws Exception {
    //
    try (var client = new EtherClient(getGethURI())) {
      client.startMiner();
      final var accountA = TestUtils.createTestAccount(client, Quantity.of(0.5, Unit.ETHER));
      final var accountB = TestUtils.createTestAccount(client, Quantity.of(0.5, Unit.ETHER));
      final var accountC = TestUtils.createTestAccount(client, Quantity.of(0.5, Unit.ETHER));
      final var accountD = TestUtils.createTestAccount(client, Quantity.of(0.5, Unit.ETHER));
      final var expirationTime = System.currentTimeMillis() / 1000 + 2; //exp 2 sec
      final var ballotAddr = BallotMock.deploy(
        client,
        accountA,
        Arrays.asList(
          accountA.address(),
          accountB.address(),
          accountC.address(),
          accountD.address()),
        expirationTime);
      final var ballot = new BallotMock(client, ballotAddr, Quantity.of(1_000_000));
      { //view function without balance
        final var balanceBefore = client.getBalance(accountA.address());
        Assert.assertFalse(ballot.isFinished(accountA.address()));
        Assert.assertEquals(balanceBefore, client.getBalance(accountA.address()));
      }
      Assert.assertFalse(ballot.isFinished(accountA.address()));
      { //vote
        ballot.vote(accountA, accountA.address());
      }
      //wait expire
      TimeUnit.SECONDS.sleep(4);
      {// finish
        ballot.finish(accountA);
        Assert.assertTrue(ballot.isFinished(accountA.address()));
        final var winner = ballot.getWinner(accountA.address());
        Assert.assertEquals(winner.voteAmount, 1);
        Assert.assertEquals(winner.address, accountA.address());
      }
    }
  }

}
