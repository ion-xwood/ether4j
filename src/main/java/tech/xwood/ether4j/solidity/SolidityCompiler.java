package tech.xwood.ether4j.solidity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.xwood.ether4j.domain.Error;
import tech.xwood.ether4j.domain.Quantity;
import tech.xwood.ether4j.json.JsonCodec;

public class SolidityCompiler {

  public static class Result {

    private final ObjectNode json;

    public Result(final ObjectNode output) {
      this.json = output;
    }

    public Quantity getByteCode(final String fileName, final String contractName) {
      if (this.json.has("errors")) {
        throw new Error(JsonCodec.toJson(this.json.get("errors"), true));
      }
      final String hex = this.json
        .get("contracts")
        .get(fileName)
        .get(contractName)
        .get("evm")
        .get("bytecode")
        .get("object")
        .asText();
      return Quantity.ofHexWithoutPrefix(hex);
    }

    public ObjectNode toJson() {
      return this.json;
    }

    @Override
    public String toString() {
      return JsonCodec.toJson(this.json, true);
    }

  }

  private static class StreamReader implements AutoCloseable {

    private final AtomicReference<StringBuilder> contentRef = new AtomicReference<>(new StringBuilder());

    private final AtomicBoolean eof = new AtomicBoolean(false);

    private final Thread thread;

    StreamReader(final InputStream stream) {
      this.thread = new Thread(() -> {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
          while (!this.eof.get()) {
            final String line = reader.readLine();
            if (line == null) {
              this.eof.set(true);
              break;
            }
            this.contentRef.get().append(line).append("\n");
          }
        }
        catch (final Exception e) {
          this.eof.set(true);
          e.printStackTrace();
        }
      });
      this.thread.start();
    }

    @Override
    public void close() {
      this.eof.set(true);
    }

    public String getContent() throws InterruptedException {
      this.thread.join();
      return this.contentRef.get().toString();
    }

  }

  public static class Task {

    private final ObjectNode json;

    public Task() {
      this.json = JsonCodec.createJsonObject();
      this.json.put("language", "Solidity");
      this.json.set("sources", JsonCodec.createJsonObject());
      final ObjectNode settings = JsonCodec.createJsonObject();
      this.json.set("settings", settings);
      final ArrayNode outTypes = JsonCodec.createJsonArray()
        .add("abi")
        .add("evm.bytecode.object")
        .add("evm.gasEstimates")
        .add("evm.methodIdentifiers");
      settings.set("outputSelection",
        JsonCodec.createJsonObject()
          .set("*", JsonCodec.createJsonObject().set("*", outTypes)));
    }

    public Task addSource(final File file) {
      try {
        final byte[] content = Files.readAllBytes(file.toPath());
        final String name = file.getName();
        return this.addSource(name, new String(content));
      }
      catch (final IOException e) {
        throw new Error(e);
      }
    }

    public Task addSource(final String name, final String content) {
      final var sol = JsonCodec.createJsonObject();
      sol.put("content", content);
      this.json.withObject("/sources").set(name, sol);
      return this;
    }

    public Task setOptimizer(final Integer optimizerLevel) {
      final ObjectNode optimizer = JsonCodec.createJsonObject();
      optimizer.put("enabled", optimizerLevel != null);
      optimizer.put("runs", optimizerLevel == null ? 0 : optimizerLevel);
      this.json.withObject("/settings").set("optimizer", optimizer);
      return this;
    }

    public ObjectNode toJson() {
      return this.json;
    }

    @Override
    public String toString() {
      return JsonCodec.toJson(this.json, true);
    }

  }

  public static SolidityCompiler create() {
    final File workDir = new File(System.getProperty("java.io.tmpdir"), "solidity");
    return create(workDir);
  }

  public static SolidityCompiler create(final File workDir) {
    final ClassLoader classLoader = SolidityCompiler.class.getClassLoader();
    final int ioBufferSizeBytes = 1024 * 1024;
    return create(workDir, classLoader, ioBufferSizeBytes);
  }

  public static SolidityCompiler create(final File workDir, final ClassLoader classLoader, final int ioBufferSizeBytes) {
    workDir.mkdirs();
    final String compilerFileName = resolveCompilerFileName();
    final String compilerResourceName = "native/0.8.21/" + compilerFileName;
    final File compilerFile = new File(workDir, compilerFileName);
    try (final InputStream fis = new BufferedInputStream(classLoader.getResourceAsStream(compilerResourceName), ioBufferSizeBytes)) {
      Files.copy(fis, compilerFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      compilerFile.setExecutable(true);
      compilerFile.deleteOnExit();
    }
    catch (final IOException e) {
      throw new Error(e);
    }
    return new SolidityCompiler(compilerFile);
  }

  private static String resolveCompilerFileName() {
    final String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains("win")) {
      return "solc-windows.exe";
    }
    else if (osName.contains("linux")) {
      return "solc-static-linux";
    }
    else if (osName.contains("mac")) {
      return "solc-macos";
    }
    else {
      throw new Error("Can't find solc compiler: unrecognized OS: " + osName);
    }
  }

  private final File compiler;

  public SolidityCompiler(final File compiler) {
    this.compiler = compiler;
  }

  public Result compile(final Task task) {
    try {
      final ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(
        this.compiler.getCanonicalPath(),
        "--standard-json"));
      processBuilder.directory(this.compiler.getParentFile());
      processBuilder.environment().put("LD_LIBRARY_PATH", this.compiler.getParentFile().getCanonicalPath());
      final Process process = processBuilder.start();
      try (final StreamReader error = new StreamReader(process.getErrorStream())) {
        try (final StreamReader output = new StreamReader(process.getInputStream())) {
          JsonCodec.toJson(process.getOutputStream(), task.toJson(), false);
          process.getOutputStream().close();
          process.waitFor();
          if (process.exitValue() != 0) {
            throw new Error(output.getContent() + "\n\n" + error.getContent());
          }
          else {
            final ObjectNode outJson = JsonCodec.fromJson(output.getContent(), ObjectNode.class);
            return new Result(outJson);
          }
        }
      }
    }
    catch (final IOException | InterruptedException e) {
      throw new Error(e);
    }
  }

  public File getCompiler() {
    return this.compiler;
  }

  @Override
  public String toString() {
    return this.compiler.toString();
  }

}
