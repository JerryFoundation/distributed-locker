package org.jerryzhu.lock.policy;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 缓存key生成规则
 * @author zhuqianchao
 * @date 2018/6/28 21:08
 */
public interface CacheKeyGenerator {

  /**
   * 获取AOP参数,生成指定缓存Key
   * @param pjp PJP
   * @return 缓存KEY
   */
  String getLockKey(ProceedingJoinPoint pjp);
}
