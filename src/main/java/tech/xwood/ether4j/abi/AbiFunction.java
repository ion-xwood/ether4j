package tech.xwood.ether4j.abi;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Collectors;
import tech.xwood.ether4j.Crypto;
import tech.xwood.ether4j.Quantity;

public class AbiFunction {

  private static int argLength(final AbiValue[] args) {
    int count = 0;
    for (final AbiValue arg : args) {
      count += (arg instanceof AbiArray) ? count += ((AbiArray) arg).values.length : 1;
    }
    return count;
  }

  public static AbiValue[] decodeResult(final Quantity data, final AbiType... resultTypes) {
    return decodeResult(data.toHexWithoutPrefix(), resultTypes);
  }

  public static AbiValue[] decodeResult(final String raw, AbiType... resultTypes) {

    if (raw.isEmpty()) {
      return new AbiValue[0];
    }
    resultTypes = (resultTypes == null) ? new AbiType[0] : resultTypes;
    final AbiValue[] outValues = new AbiValue[resultTypes.length];

    int offset = 0;
    for (int i = 0; i < resultTypes.length; i++) {

      final AbiType type = resultTypes[i];
      final int dataOffset = getDataOffset(raw, offset, type);

      if (type instanceof AbiArrayDynamic.Type) {
        outValues[i] = type.decode(raw, dataOffset);
        offset += Abi.MAX_BYTE_LENGTH_FOR_HEX_STRING;
      }
      else if (type instanceof AbiArray.Type) {
        outValues[i] = type.decode(raw, dataOffset);
        final int length = ((AbiArray.Type) type).length;
        offset += length * Abi.MAX_BYTE_LENGTH_FOR_HEX_STRING;
      }
      else {
        outValues[i] = type.decode(raw, dataOffset);
        offset += Abi.MAX_BYTE_LENGTH_FOR_HEX_STRING;
      }
    }
    return outValues;
  }

  static void encodeArgs(final StringBuilder dest, final AbiValue[] args) {

    int dynamicDataOffset = argLength(args) * Abi.MAX_BYTE_LENGTH;
    final StringBuilder dynamicData = new StringBuilder();
    for (final AbiValue arg : args) {
      final String encodedValue = arg.encode();
      if ((arg instanceof AbiBytesDynamic) || (arg instanceof AbiString) || (arg instanceof AbiArrayDynamic)) {
        final String encodedDataOffset = AbiUint.encode(BigInteger.valueOf(dynamicDataOffset));
        dest.append(encodedDataOffset);
        dynamicData.append(encodedValue);
        dynamicDataOffset += encodedValue.length() >> 1;
      }
      else {
        dest.append(encodedValue);
      }
    }
    dest.append(dynamicData);
  }

  public static String encodeCall(final String name, final AbiValue... args) {
    final StringBuilder dest = new StringBuilder();
    encodeCallTo(dest, name, args);
    return dest.toString();
  }

  public static Quantity encodeCallAsQuantity(final String name, final AbiValue... args) {
    return Quantity.ofHexWithoutPrefix(encodeCall(name, args));
  }

  public static void encodeCallTo(final StringBuilder dest, final String name, AbiValue... args) {

    args = args == null ? new AbiValue[0] : args;

    final AbiType[] argTypes = Arrays.stream(args)
      .map(v -> v.type)
      .toArray(size -> new AbiType[size]);

    dest.append(getId(name, argTypes));
    encodeArgs(dest, args);
  }

  public static String encodeConstructorCall(final AbiValue... args) {
    final StringBuilder dest = new StringBuilder();
    encodeConstructorCallTo(dest, args);
    return dest.toString();
  }

  public static void encodeConstructorCallTo(final StringBuilder dest, final AbiValue... args) {
    encodeArgs(dest, args);
  }

  private static int getDataOffset(final String raw, final int offset, final AbiType type) {

    if (type instanceof AbiBytesDynamic.Type || type instanceof AbiString.Type || type instanceof AbiArrayDynamic.Type) {
      return AbiUint.Type.decode(raw, offset, 256).intValue() << 1;
    }
    return offset;
  }

  public static String getId(final String name, final AbiType... argTypes) {
    final byte[] input = getSignature(name, argTypes).getBytes();
    final byte[] hash = Crypto.keccak256(input);
    return Abi.toHex(hash).substring(0, 8);
  }

  public static String getSignature(final String name, final AbiType... argTypes) {
    final StringBuilder result = new StringBuilder(name.length() + argTypes.length * 32);
    result.append(name);
    result.append("(");
    final String params = Arrays.stream(argTypes)
      .map(t -> t.name)
      .collect(Collectors.joining(","));
    result.append(params);
    result.append(")");
    return result.toString();
  }
}
