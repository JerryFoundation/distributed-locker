package org.jerryzhu.lock.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jerryzhu.lock.annotation.CacheLock;
import org.jerryzhu.lock.exception.CacheLockException;
import org.jerryzhu.lock.policy.CacheKeyGenerator;
import org.jerryzhu.lock.util.RedisLockHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 拦截器
 * @author zhuqianchao
 * @date 2018/6/29 11:17
 */
@Aspect
@Configuration
public class LockMethodInterceptor {

  @Autowired
  public LockMethodInterceptor(RedisLockHelper redisLockHelper, CacheKeyGenerator cacheKeyGenerator) {
    this.redisLockHelper = redisLockHelper;
    this.cacheKeyGenerator = cacheKeyGenerator;
  }

  private final RedisLockHelper redisLockHelper;
  private final CacheKeyGenerator cacheKeyGenerator;


  @Around("execution(public * *(..)) && @annotation(org.jerryzhu.lock.annotation.CacheLock)")
  public Object interceptor(ProceedingJoinPoint pjp) {
    MethodSignature signature = (MethodSignature) pjp.getSignature();
    Method method = signature.getMethod();
    CacheLock lock = method.getAnnotation(CacheLock.class);
    if (StringUtils.isEmpty(lock.prefix())) {
      throw new CacheLockException(300, "锁key值不能为空");
    }
    final String lockKey = cacheKeyGenerator.getLockKey(pjp);
    String value = UUID.randomUUID().toString();
    try {
      // 假设上锁成功，但是设置过期时间失效，以后拿到的都是 false
      final boolean success = redisLockHelper.lockWithNoRefreshTimeOut(lockKey, value, lock.expire(), lock.timeUnit());
      if (!success) {
        throw new CacheLockException(200, "重复提交");
      }
      try {
        return pjp.proceed();
      } catch (Throwable throwable) {
        throw new CacheLockException(100, "系统异常");
      }
    } finally {
      // 释放锁
      redisLockHelper.unlock(lockKey, value);
    }
  }
}
