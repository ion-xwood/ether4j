package tech.xwood.geth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

public class GethTestContainer implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(GethTestContainer.class);

  public static GethTestContainer create() {
    return new GethTestContainer("ethereum/client-go:release-1.12");
  }

  public static GethTestContainer create(final String dockerImage) {
    return new GethTestContainer(dockerImage);
  }

  private static GenericContainer<?> createContainer(final String dockerImage, final int containerPort) {
    final var container = new GenericContainer<>(dockerImage);
    container.withEnv("MALLOC_ARENA_MAX", "1");
    container.withLogConsumer(new Slf4jLogConsumer(LOGGER));
    container.withExposedPorts(containerPort);
    container.withCommand(
      "--dev",
      "--http",
      "--http.addr=0.0.0.0",
      "--http.port=" + containerPort,
      "--http.corsdomain=*",
      "--http.api=admin,debug,miner,shh,txpool,personal,eth,net,web3,db",
      "--ipcdisable",
      "--rpc.allow-unprotected-txs");
    container.waitingFor(Wait.forLogMessage(".*HTTP server started.*", 1));
    container.start();
    return container;
  }

  private final GenericContainer<?> container;

  private final String host;

  private final int port;

  private GethTestContainer(final String dockerImage) {
    final var containerPort = 8545;
    this.container = createContainer(dockerImage, containerPort);
    this.host = this.container.getHost();
    this.port = this.container.getMappedPort(containerPort);
  }

  @Override
  public void close() {
    try {
      this.container.close();
    }
    catch (final Exception e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  public String getHost() {
    return this.host;
  }

  public int getPort() {
    return this.port;
  }

}
