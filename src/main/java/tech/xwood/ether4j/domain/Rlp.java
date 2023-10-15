package tech.xwood.ether4j.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rlp {

  public interface Type {
  }

  public static class TypeList implements Type {

    private final List<Type> items;

    private TypeList(final List<Type> items) {
      this.items = items;
    }

    public TypeList add(final Type type) {
      items.add(type);
      return this;
    }

    public TypeList getList(final int index) {
      return (TypeList) items.get(index);
    }

    public TypeString getString(final int index) {
      return (TypeString) items.get(index);
    }

    public List<Type> items() {
      return items;
    }

  }

  public static class TypeString implements Type {

    private final byte[] bytes;

    private TypeString(final byte[] value) {
      bytes = value;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final TypeString other = (TypeString) obj;
      return Arrays.equals(bytes, other.bytes);
    }

    public byte[] getBytes() {
      return bytes;
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(bytes);
    }

  }

  private static final int OFFSET_SHORT_STRING = 0x80;

  private static final int OFFSET_LONG_STRING = 0xb7;

  private static final int OFFSET_SHORT_LIST = 0xc0;

  private static final int OFFSET_LONG_LIST = 0xf7;

  private static int calcLength(final int lengthOfLength, final byte[] data, final int pos) {
    byte pow = (byte) (lengthOfLength - 1);
    int length = 0;
    for (int i = 1; i <= lengthOfLength; ++i) {
      length += (data[pos + i] & 0xff) << (8 * pow);
      pow--;
    }
    return length;
  }

  private static byte[] concat(final byte[] b1, final byte[] b2) {
    final byte[] result = Arrays.copyOf(b1, b1.length + b2.length);
    System.arraycopy(b2, 0, result, b1.length, b2.length);
    return result;
  }

  public static TypeString createEmptyString() {
    return new TypeString(new byte[] {});
  }

  public static TypeList createList() {
    return new TypeList(new ArrayList<>());
  }

  public static TypeList createList(final List<Type> items) {
    return new TypeList(new ArrayList<>(items));
  }

  public static TypeString createString(final byte value) {
    return new TypeString(new byte[] { value });
  }

  public static TypeString createString(final byte[] bytes) {
    return new TypeString(bytes);
  }

  public static TypeString createString(final Quantity quantity) {
    return quantity == null ? createEmptyString() : new TypeString(quantity.toBytes());
  }

  public static TypeString createString(final String string) {
    return new TypeString(string.getBytes());
  }

  public static TypeList decode(final byte[] rlpEncoded) {
    final TypeList rlpList = createList();
    decode(rlpEncoded, 0, rlpEncoded.length, rlpList);
    return rlpList;
  }

  private static void decode(final byte[] data, int startPos, final int endPos, final TypeList list) {
    if (data == null || data.length == 0) {
      return;
    }
    while (startPos < endPos) {
      final int prefix = data[startPos] & 0xff;
      if (prefix < OFFSET_SHORT_STRING) {
        final byte[] rlpData = { (byte) prefix };
        list.items.add(new TypeString(rlpData));
        startPos += 1;
      }
      else if (prefix == OFFSET_SHORT_STRING) {
        list.items.add(new TypeString(new byte[0]));
        startPos += 1;
      }
      else if (prefix > OFFSET_SHORT_STRING && prefix <= OFFSET_LONG_STRING) {
        final byte strLen = (byte) (prefix - OFFSET_SHORT_STRING);
        final byte[] rlpData = new byte[strLen];
        System.arraycopy(data, startPos + 1, rlpData, 0, strLen);
        list.items.add(new TypeString(rlpData));
        startPos += 1 + strLen;
      }
      else if (prefix > OFFSET_LONG_STRING && prefix < OFFSET_SHORT_LIST) {
        final byte lenOfStrLen = (byte) (prefix - OFFSET_LONG_STRING);
        final int strLen = calcLength(lenOfStrLen, data, startPos);
        final byte[] rlpData = new byte[strLen];
        System.arraycopy(data, startPos + lenOfStrLen + 1, rlpData, 0, strLen);
        list.items.add(new TypeString(rlpData));
        startPos += lenOfStrLen + strLen + 1;
      }
      else if (prefix >= OFFSET_SHORT_LIST && prefix <= OFFSET_LONG_LIST) {
        final byte listLen = (byte) (prefix - OFFSET_SHORT_LIST);
        final TypeList newLevelList = createList();
        decode(data, startPos + 1, startPos + listLen + 1, newLevelList);
        list.items.add(newLevelList);
        startPos += 1 + listLen;
      }
      else if (prefix > OFFSET_LONG_LIST) {
        final byte lenOfListLen = (byte) (prefix - OFFSET_LONG_LIST);
        final int listLen = calcLength(lenOfListLen, data, startPos);
        final TypeList newLevelList = createList();
        decode(data, startPos + lenOfListLen + 1, startPos + lenOfListLen + listLen + 1, newLevelList);
        list.items.add(newLevelList);
        startPos += lenOfListLen + listLen + 1;
      }
    }
  }

  private static byte[] encode(final byte[] bytesValue, final int offset) {
    if (bytesValue.length == 1
      && offset == OFFSET_SHORT_STRING
      && bytesValue[0] >= (byte) 0x00
      && bytesValue[0] <= (byte) 0x7f) {
      return bytesValue;
    }
    else if (bytesValue.length <= 55) {
      final byte[] result = new byte[bytesValue.length + 1];
      result[0] = (byte) (offset + bytesValue.length);
      System.arraycopy(bytesValue, 0, result, 1, bytesValue.length);
      return result;
    }
    else {
      final byte[] encodedStringLength = toMinimalByteArray(bytesValue.length);
      final byte[] result = new byte[bytesValue.length + encodedStringLength.length + 1];
      result[0] = (byte) ((offset + 0x37) + encodedStringLength.length);
      System.arraycopy(encodedStringLength, 0, result, 1, encodedStringLength.length);
      System.arraycopy(
        bytesValue, 0, result, encodedStringLength.length + 1, bytesValue.length);
      return result;
    }
  }

  public static byte[] encode(final Type value) {
    if (value instanceof TypeString) {
      return encodeString((TypeString) value);
    }
    else {
      return encodeList((TypeList) value);
    }
  }

  private static byte[] encodeList(final TypeList list) {
    final List<Type> items = list.items;
    if (items.isEmpty()) {
      return encode(new byte[] {}, OFFSET_SHORT_LIST);
    }
    else {
      byte[] result = new byte[0];
      for (final Type entry : items) {
        result = concat(result, encode(entry));
      }
      return encode(result, OFFSET_SHORT_LIST);
    }
  }

  private static byte[] encodeString(final TypeString string) {
    return encode(string.bytes, OFFSET_SHORT_STRING);
  }

  private static byte[] toByteArray(final int value) {
    return new byte[] {
      (byte) ((value >> 24) & 0xff),
      (byte) ((value >> 16) & 0xff),
      (byte) ((value >> 8) & 0xff),
      (byte) (value & 0xff)
    };
  }

  private static byte[] toMinimalByteArray(final int value) {
    final byte[] encoded = toByteArray(value);
    for (int i = 0; i < encoded.length; i++) {
      if (encoded[i] != 0) {
        return Arrays.copyOfRange(encoded, i, encoded.length);
      }
    }
    return new byte[] {};
  }

}
