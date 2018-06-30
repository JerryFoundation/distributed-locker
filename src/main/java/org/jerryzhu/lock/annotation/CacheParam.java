package org.jerryzhu.lock.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 锁的参数
 * @author zhuqianchao
 * @date 2018/6/28 21:05
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CacheParam {
  /**
   * 字段名称
   *
   * @return String
   */
  @AliasFor("value")
  String name() default "";

  /**
   * <p>见name<p/>
   */
  @AliasFor("name")
  String value() default "";
}
