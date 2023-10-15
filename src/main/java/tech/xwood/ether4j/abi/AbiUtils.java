package tech.xwood.ether4j.abi;

import tech.xwood.ether4j.domain.Error;

class AbiUtils {

  public static void require(final boolean condition, final String message) {
    if (!condition) {
      throw new Error(message);
    }
  }

}
