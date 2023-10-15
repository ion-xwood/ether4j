package tech.xwood.ether4j.domain;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RlpTest {

  @Test
  public void testEncodeDecode() {
    final Rlp.TypeList list = Rlp.createList(Arrays.asList(
      Rlp.createString("a"),
      Rlp.createString("b"),
      Rlp.createString("c")));
    final byte[] encoded = Rlp.encode(list);
    Assert.assertEquals(encoded, new byte[] { -61, 97, 98, 99 });
    final Rlp.TypeList decoded = Rlp.decode(encoded).getList(0);
    Assert.assertEquals(decoded.getString(0), Rlp.createString("a"));
    Assert.assertEquals(decoded.getString(1), Rlp.createString("b"));
    Assert.assertEquals(decoded.getString(2), Rlp.createString("c"));
  }

}
