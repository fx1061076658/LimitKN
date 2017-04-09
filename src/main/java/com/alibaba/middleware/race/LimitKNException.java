package com.alibaba.middleware.race;

/**
 * LimitKN异常
 *
 * @author xionghui
 * @since 1.0.0
 */
public class LimitKNException extends RuntimeException {
  private static final long serialVersionUID = 8862862314238521885L;

  public LimitKNException() {
    super();
  }

  public LimitKNException(String message) {
    super(message);
  }

  public LimitKNException(String message, Throwable cause) {
    super(message, cause);
  }

  public LimitKNException(Throwable cause) {
    super(cause);
  }
}
