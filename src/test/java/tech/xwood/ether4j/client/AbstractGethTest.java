package tech.xwood.ether4j.client;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import tech.xwood.geth.GethTestContainer;

abstract class AbstractGethTest {

  private static volatile GethTestContainer GETH_TEST_CONTAINER;

  protected static String getGethURI() {
    return String.format("http://%s:%s", GETH_TEST_CONTAINER.getHost(), GETH_TEST_CONTAINER.getPort());
  }

  @AfterSuite
  protected void afterSuite() {
    GETH_TEST_CONTAINER.close();
    GETH_TEST_CONTAINER = null;
  }

  @BeforeSuite
  protected void beforeSuite() {
    GETH_TEST_CONTAINER = GethTestContainer.create();
  }

}
