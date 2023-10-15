package tech.xwood.ether4j.abi;

import java.util.ArrayList;
import java.util.List;
import tech.xwood.ether4j.domain.Crypto;
import tech.xwood.ether4j.domain.Error;
import tech.xwood.ether4j.domain.Quantity;

public class AbiEvent {

  public static class Types {

    public static class Item {

      public final AbiType type;

      public final boolean indexed;

      public Item(final AbiType type, final boolean indexed) {
        this.type = type;
        this.indexed = indexed;
      }

    }

    public static Types create() {
      return new Types();
    }

    public final List<Item> items;

    private Types() {
      items = new ArrayList<>();
    }

    public Types add(final AbiType type) {
      return add(type, false);
    }

    public Types add(final AbiType type, final boolean indexed) {
      items.add(new Item(type, indexed));
      return this;
    }

    public AbiType[] getNotIndexedTypes() {
      return items.stream()
        .filter(i -> !i.indexed)
        .map(i -> i.type)
        .toArray(s -> new AbiType[s]);
    }

    public AbiType[] getTypes() {
      return items.stream()
        .map(i -> i.type)
        .toArray(s -> new AbiType[s]);
    }

    public Types indexed(final AbiType type) {
      return add(type, true);
    }

  }

  public static AbiValue[] decode(final Quantity data, final Quantity[] topics, final String eventName, final Types types) {
    final Quantity signature = AbiEvent.getSignatureAsQuantity(eventName, types.getTypes());
    if (!signature.equals(topics[0])) {
      throw new Error("Event signature is wrong");
    }
    final AbiValue[] notIndexedValues = AbiEvent.decodeNotIndexedValues(data, types.getNotIndexedTypes());
    final int size = types.items.size();
    final AbiValue[] result = new AbiValue[size];
    int resultIndex = 0;
    int topicsIndex = 1;
    int notIndexedValuesIndex = 0;
    for (final Types.Item item : types.items) {
      if (item.indexed) {
        result[resultIndex++] = decodeIndexedValue(topics[topicsIndex++], item.type);
      }
      else {
        result[resultIndex++] = notIndexedValues[notIndexedValuesIndex++];
      }
    }
    return result;
  }

  public static AbiValue decodeIndexedValue(final Quantity raw, final AbiType type) {
    return decodeIndexedValue(raw.toHexWithoutPrefix(), type);
  }

  public static AbiValue decodeIndexedValue(final Quantity raw, final int offset, final AbiType type) {
    return decodeIndexedValue(raw.toHexWithoutPrefix(), offset, type);
  }

  public static AbiValue decodeIndexedValue(final String raw, final AbiType type) {
    return decodeIndexedValue(raw, 0, type);
  }

  public static AbiValue decodeIndexedValue(final String raw, final int offset, final AbiType type) {
    return type.decode(raw, offset);
  }

  public static AbiValue[] decodeNotIndexedValues(final Quantity data, final AbiType... resultTypes) {
    return AbiFunction.decodeResult(data, resultTypes);
  }

  public static String encode(final String name, final AbiType... argTypes) {
    final StringBuilder dest = new StringBuilder();
    encodeTo(dest, name, argTypes);
    return dest.toString();
  }

  public static void encodeTo(final StringBuilder dest, final String name, final AbiType... argTypes) {
    final byte[] input = getSignature(name, argTypes).getBytes();
    final byte[] hash = Crypto.keccak256(input);
    Abi.hexTo(dest, hash);
  }

  public static String getSignature(final String name, final AbiType... argTypes) {
    final byte[] input = AbiFunction.getSignature(name, argTypes).getBytes();
    final byte[] hash = Crypto.keccak256(input);
    return Abi.toHex(hash);
  }

  public static Quantity getSignatureAsQuantity(final String name, final AbiType... argTypes) {
    return Quantity.ofHexWithoutPrefix(getSignature(name, argTypes));
  }

}
