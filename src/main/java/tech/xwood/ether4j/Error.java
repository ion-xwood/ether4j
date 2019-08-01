package tech.xwood.ether4j;

public class Error extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final int code;

  public Error(final int code, final String message) {
    super(message);
    this.code = code;
  }

  public Error(final String message) {
    super(message);
    code = -1;
  }

  public Error(final String message, final Throwable e) {
    super(message, e);
    code = -1;
  }

  public Error(final Throwable e) {
    super(e);
    code = -1;
  }

  public int getCode() {
    return code;
  }
}