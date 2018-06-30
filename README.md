# distributed-locker
注解方式实现分布式锁

* 将jar包引入

* 在需要添加分布式锁的方法上加上@CacheLock(prefix="xxx") , prefix为key的前缀

* 需要组成key的参数使用 @CacheParam 如  @CacheParam("token")

* 完成
