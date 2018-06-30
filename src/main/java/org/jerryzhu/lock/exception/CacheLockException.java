package org.jerryzhu.lock.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 缓存锁异常
 * @author zhuqianchao
 * @date 2018/6/29 11:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CacheLockException extends RuntimeException {
  /**
   * 错误码
   * @author zhuqianchao
   */
  private int code;

  public CacheLockException() {
    super();
  }

  public CacheLockException(int code, String message) {
    super(message);
    this.setCode(code);
  }
}
