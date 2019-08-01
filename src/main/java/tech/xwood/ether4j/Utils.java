package tech.xwood.ether4j;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

  private static final ObjectWriter WRITER_PRETTY;
  private static final ObjectWriter WRITER;
  private static final ObjectMapper MAPPER;
  static {
    MAPPER = new ObjectMapper();
    MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    MAPPER.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    MAPPER.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
    MAPPER.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
    MAPPER.setSerializationInclusion(Include.NON_EMPTY);
    MAPPER.setVisibility(MAPPER.getSerializationConfig().getDefaultVisibilityChecker()
      .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
      .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
      .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
      .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    WRITER = MAPPER.writer();
    MAPPER.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
    WRITER_PRETTY = MAPPER.writer().withDefaultPrettyPrinter();
  }

  public static ArrayNode createJsonArray() {
    return MAPPER.createArrayNode();
  }

  public static ObjectNode createJsonObject() {
    return MAPPER.createObjectNode();
  }

  public static <T> T fromJson(final InputStream jsonInputStream, final Class<T> targetType) {
    try {
      return MAPPER.readValue(jsonInputStream, targetType);
    }
    catch (final IOException e) {
      throw new Error(e);
    }
  }

  public static <T> T fromJson(final JsonNode jsonNode, final Class<T> targetType) {
    return MAPPER.convertValue(jsonNode, targetType);
  }

  public static <T> T fromJson(final String json, final Class<T> targetType) {
    try {
      return MAPPER.readValue(json, targetType);
    }
    catch (final IOException e) {
      throw new Error(e);
    }
  }

  public static void require(final boolean condition, final String message) {
    if (!condition) {
      throw new Error(message);
    }
  }

  public static String toJson(final Object value) {
    return toJson(value, false);
  }

  public static String toJson(final Object value, final boolean pretty) {
    try {
      return pretty ? WRITER_PRETTY.writeValueAsString(value) : WRITER.writeValueAsString(value);
    }
    catch (final JsonProcessingException e) {
      throw new Error(e);
    }
  }

  public static void toJson(final OutputStream outputStream, final Object value, final boolean pretty) {
    try {
      if (pretty) {
        WRITER_PRETTY.writeValue(outputStream, value);
      }
      else {
        WRITER.writeValue(outputStream, value);
      }
    }
    catch (final IOException e) {
      throw new Error(e);
    }
  }

  public static byte[] toJsonAsBytes(final Object value, final boolean pretty) {
    try {
      return pretty ? WRITER_PRETTY.writeValueAsBytes(value) : WRITER.writeValueAsBytes(value);
    }
    catch (final JsonProcessingException e) {
      throw new Error(e);
    }
  }

}
