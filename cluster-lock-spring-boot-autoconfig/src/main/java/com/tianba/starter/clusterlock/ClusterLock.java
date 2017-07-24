package com.tianba.starter.clusterlock;

import java.lang.annotation.*;

/**
 * 基于注解的分布式简单锁实现
 * <p>
 * key 配置锁键前缀，一般根据业务配置，统一业务建议配置相同key
 * value 锁键生成策略基于SPEL(#引用方法参数，@引用spring bean)(eg:@testService.value+#user.name+#user.loves[1]+#max)
 *
 * @author 曹金桂 2017年06月20日16:49:38
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ClusterLock {

    String value() default "";

    String key() default "";

    long invalidTime() default 2; //锁自动释放时间(秒)

    long waitTime() default -1;//等待自旋时间
}
