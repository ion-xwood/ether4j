package tech.xwood.ether4j;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SolidityCompiler {

  public static class Result {

    private final ObjectNode json;

    public Result(final ObjectNode output) {
      json = output;
    }

    public Quantity getByteCode(final String fileName, final String contractName) {
      if (json.has("errors")) {
        throw new Error(Utils.toJson(json.get("errors"), true));
      }
      final String hex = json
        .with("contracts")
        .with(fileName)
        .with(contractName)
        .with("evm")
        .with("bytecode")
        .get("object")
        .asText();
      return Quantity.ofHexWithoutPrefix(hex);
    }

    public ObjectNode toJson() {
      return json;
    }

    @Override
    public String toString() {
      return Utils.toJson(json, true);
    }
  }

  private static class StreamReader implements AutoCloseable {

    private final AtomicReference<StringBuilder> contentRef = new AtomicReference<>(new StringBuilder());
    private final AtomicBoolean eof = new AtomicBoolean(false);
    private final Thread thread;

    StreamReader(final InputStream stream) {

      thread = new Thread(() -> {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
          while (!eof.get()) {
            final String line = reader.readLine();
            if (line == null) {
              eof.set(true);
              break;
            }
            contentRef.get().append(line).append("\n");
          }
        }
        catch (final Exception e) {
          eof.set(true);
          e.printStackTrace();
        }

      });
      thread.start();
    }

    @Override
    public void close() {
      eof.set(true);
    }

    public String getContent() throws InterruptedException {
      thread.join();
      return contentRef.get().toString();
    }

  }

  public static class Task {

    private final ObjectNode json;

    public Task() {
      json = Utils.createJsonObject();
      json.put("language", "Solidity");
      json.set("sources", Utils.createJsonObject());

      final ObjectNode settings = Utils.createJsonObject();
      json.set("settings", settings);
      final ArrayNode outTypes = Utils.createJsonArray()
        .add("abi")
        .add("evm.bytecode.object")
        .add("evm.gasEstimates")
        .add("evm.methodIdentifiers");
      settings.set("outputSelection",
        Utils.createJsonObject()
          .set("*", Utils.createJsonObject().set("*", outTypes)));
    }

    public Task addSource(final File file) {
      try {
        final byte[] content = Files.readAllBytes(file.toPath());
        final String name = file.getName();
        return addSource(name, new String(content));
      }
      catch (final IOException e) {
        throw new Error(e);
      }
    }

    public Task addSource(final String name, final String content) {
      final ObjectNode sol = Utils.createJsonObject();
      sol.put("content", content);
      json.with("sources").set(name, sol);
      return this;
    }

    public Task setOptimizer(final Integer optimizerLevel) {

      final ObjectNode optimizer = Utils.createJsonObject();
      optimizer.put("enabled", optimizerLevel != null);
      optimizer.put("runs", optimizerLevel == null ? 0 : optimizerLevel);
      json.with("settings").set("optimizer", optimizer);
      return this;
    }

    public ObjectNode toJson() {
      return json;
    }

    @Override
    public String toString() {
      return Utils.toJson(json, true);
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
    final String osName = System.getProperty("os.name").toLowerCase();
    final String os = osName.contains("win") ? "win" : osName.contains("linux") ? "linux" : osName.contains("mac") ? "mac" : null;
    if (os == null) {
      throw new Error("Can't find solc compiler: unrecognized OS: " + os);
    }
    final String fileListResourceName = String.format("native/%s/solc/file.list", os);
    try (final InputStream is = new BufferedInputStream(classLoader.getResourceAsStream(fileListResourceName), ioBufferSizeBytes)) {
      try (Scanner scanner = new Scanner(is)) {
        while (scanner.hasNext()) {
          final String fileName = scanner.next();
          final File targetFile = new File(workDir, fileName);
          final String srcFileResourceName = String.format("native/%s/solc/%s", os, fileName);
          try (final InputStream fis = new BufferedInputStream(classLoader.getResourceAsStream(srcFileResourceName), ioBufferSizeBytes)) {
            Files.copy(fis, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            targetFile.setExecutable(true);
            targetFile.deleteOnExit();
          }
        }
      }
    }
    catch (final IOException e) {
      throw new Error(e);
    }
    final String compilerFileName = os.equals("win") ? "solc.exe" : "solc";
    final File compiler = new File(workDir, compilerFileName);
    return new SolidityCompiler(compiler);
  }

  private final File compiler;

  public SolidityCompiler(final File compiler) {
    this.compiler = compiler;
  }

  public Result compile(final Task task) {
    try {
      final ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(
        compiler.getCanonicalPath(),
        "--standard-json"));

      processBuilder.directory(compiler.getParentFile());
      processBuilder.environment().put("LD_LIBRARY_PATH", compiler.getParentFile().getCanonicalPath());

      final Process process = processBuilder.start();
      try (final StreamReader error = new StreamReader(process.getErrorStream())) {
        try (final StreamReader output = new StreamReader(process.getInputStream())) {
          Utils.toJson(process.getOutputStream(), task.toJson(), false);
          process.getOutputStream().close();
          process.waitFor();
          if (process.exitValue() != 0) {
            throw new Error(output.getContent() + "\n\n" + error.getContent());
          }
          else {
            final ObjectNode outJson = Utils.fromJson(output.getContent(), ObjectNode.class);
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
    return compiler;
  }

  @Override
  public String toString() {
    return compiler.toString();
  }

}
