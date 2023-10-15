package tech.xwood.ether4j.solidity;

import java.io.File;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SolidityCompilerTest {

  @Test
  public void testCompiler() throws Exception {
    final var compiler = SolidityCompiler.create();
    final var input = new SolidityCompiler.Task()
      .addSource(new File("src/test/resources/sol/Ballot.sol"))
      .setOptimizer(2);
    final var output = compiler.compile(input);
    final var byteCode = output.getByteCode("Ballot.sol", "Ballot");
    Assert.assertNotNull(byteCode);
  }

  @Test
  public void testCreate() {
    final var solidity = SolidityCompiler.create();
    Assert.assertTrue(solidity.getCompiler().exists());
  }

}
