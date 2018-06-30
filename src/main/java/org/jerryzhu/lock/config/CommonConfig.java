package org.jerryzhu.lock.config;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 基础配置
 * @author zhuqianchao
 * @date 2018/6/29 16:07
 */
@Configuration
public class CommonConfig {

  @Bean
  public ScheduledThreadPoolExecutor scheduledThreadPoolExecutor() {
    return new ScheduledThreadPoolExecutor(10,
            new BasicThreadFactory.Builder().daemon(true).namingPattern("redis-lock-").build());
  }
}
