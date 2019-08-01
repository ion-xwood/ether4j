package tech.xwood.ether4j;

import java.io.File;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSolidityCompiler {

  @Test
  public void testCompiler() throws Exception {

    final SolidityCompiler compiler = SolidityCompiler.create();

    final SolidityCompiler.Task input = new SolidityCompiler.Task()
      .addSource(new File("src/sol/Ballot.sol"))
      .setOptimizer(2);

    final SolidityCompiler.Result output = compiler.compile(input);
    final Quantity byteCode = output.getByteCode("Ballot.sol", "Ballot");
    Assert.assertNotNull(byteCode);
  }

  @Test
  public void testCreate() {
    final SolidityCompiler solidity = SolidityCompiler.create();
    Assert.assertTrue(solidity.getCompiler().exists());
  }
}
