package tech.xwood.ether4j.abi;

public class Abi {

  static final int MAX_BIT_LENGTH = 256;
  static final int MAX_BYTE_LENGTH = MAX_BIT_LENGTH / 8;
  static final int MAX_BYTE_LENGTH_FOR_HEX_STRING = MAX_BYTE_LENGTH << 1;

  static void hexTo(final StringBuilder dest, final byte[] input) {
    for (final byte element : input) {
      dest.append(String.format("%02x", element & 0xFF));
    }
  }

  static byte[] hexToBytes(final String hex) {

    final int len = hex.length();
    if (len == 0) {
      return new byte[] {};
    }
    byte[] data;
    int startIdx;
    if (len % 2 != 0) {
      data = new byte[(len / 2) + 1];
      data[0] = (byte) Character.digit(hex.charAt(0), 16);
      startIdx = 1;
    }
    else {
      data = new byte[len / 2];
      startIdx = 0;
    }
    for (int i = startIdx; i < len; i += 2) {
      data[(i + 1) / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
    }
    return data;
  }

  static String toHex(final byte[] input) {
    final StringBuilder dest = new StringBuilder();
    hexTo(dest, input);
    return dest.toString();
  }
}
