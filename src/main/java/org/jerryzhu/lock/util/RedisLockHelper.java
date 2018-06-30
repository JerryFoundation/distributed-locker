package org.jerryzhu.lock.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author zhuqianchao
 * @date 2018/6/29 15:57
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@Slf4j
public class RedisLockHelper {

  private static final String DELIMITER = "|";

  @Resource
  private ScheduledThreadPoolExecutor schedule;

  private final StringRedisTemplate stringRedisTemplate;

  public RedisLockHelper(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  /**
   * 获取锁
   *
   * @param lockKey lockKey
   * @param uuid    UUID
   * @param timeout 超时时间
   * @param unit    过期单位
   * @return true or false
   */
  public Boolean lock(String lockKey, final String uuid, long timeout, final TimeUnit unit) {
    final long milliseconds = Expiration.from(timeout, unit).getExpirationTimeInMilliseconds();
    Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, (System.currentTimeMillis() + milliseconds) + DELIMITER + uuid);
    if (success) {
      stringRedisTemplate.expire(lockKey, timeout, TimeUnit.SECONDS);
    } else {
      String oldVal = stringRedisTemplate.opsForValue().getAndSet(lockKey, (System.currentTimeMillis() + milliseconds) + DELIMITER + uuid);
      final String[] oldValues = oldVal.split(Pattern.quote(DELIMITER));
      if (Long.parseLong(oldValues[0]) + 1L <= System.currentTimeMillis()) {
        return true;
      }
    }
    return success;
  }

  /**
   * 获取锁, 但是不刷新
   *
   * @param lockKey lockKey
   * @param uuid    UUID
   * @param timeout 超时时间
   * @param unit    过期单位
   * @return true or false
   */
  public Boolean lockWithNoRefreshTimeOut(String lockKey, final String uuid, long timeout, final TimeUnit unit) {
    final long milliseconds = Expiration.from(timeout, unit).getExpirationTimeInMilliseconds();
    Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, (System.currentTimeMillis() + milliseconds) + DELIMITER + uuid);
    if (success) {
      stringRedisTemplate.expire(lockKey, timeout, TimeUnit.SECONDS);
    }
    return success;
  }


  /**
   * @see <a href="http://redis.io/commands/set">Redis Documentation: SET</a>
   */
  public void unlock(String lockKey, String value) {
    unlock(lockKey, value, 0, TimeUnit.MILLISECONDS);
  }

  /**
   * 延迟unlock
   *
   * @param lockKey   key
   * @param uuid      client(最好是唯一键的)
   * @param delayTime 延迟时间
   * @param unit      时间单位
   */
  public void unlock(final String lockKey, final String uuid, long delayTime, TimeUnit unit) {
    if (StringUtils.isEmpty(lockKey)) {
      return;
    }
    if (delayTime <= 0) {
      doUnlock(lockKey, uuid);
    } else {
      schedule.schedule(() -> doUnlock(lockKey, uuid), delayTime, unit);
    }
  }

  /**
   * @param lockKey key
   * @param uuid    client(最好是唯一键的)
   */
  private void doUnlock(final String lockKey, final String uuid) {
    String value = stringRedisTemplate.opsForValue().get(lockKey);
    final String[] values = value.split(Pattern.quote(DELIMITER));
    if (Objects.equals(values[1], uuid)) {
      log.info("【删除锁开始】 lockKey:{}", lockKey);
      stringRedisTemplate.delete(lockKey);
      log.info("【删除锁结束】 lockKey:{}", lockKey);
    }
  }
}
