# cluster-lock-spring-boot-starter
基于Redis实现的简单分布式锁

> 如有建议或意见欢迎指教 cao_jingui@163.com

## 概述

通常在业务开发时候，尽量保证接口的调用幂等。比如，对同一个订单用户只能评价一次，如果用户评价两次则第二次应该返回异常。
但客户端存在短时间内按钮重复点击问题。这个时候由于后台集群部署，两个请求发送到不同的实例进行处理。那么在查询数据库进行判断是否已经评价会存在并问题。
简单的方案是采用redis实现一个简单的分布式锁，在第一个请求拿到之后，先获取锁，第二个请求再获取锁将失败，这样保证一个只处理一个请求。

本项目基于Spring Boot开发的starter，使用条件项目必须注入RedisTemplate对象。

## 使用方法

1. jar包引用
```
<dependency>
   <groupId>com.tianba</groupId>
   <artifactId>cluster-lock-spring-boot-starter</artifactId>
   <version>1.0-SNAPSHOT</version>
</dependency>
```

2. 在需要进行分布式锁控制的方法添加@ClusterLock注解，value值基于SPEL表达式策略生成锁的键。

3. 基本配置

```
cluster.lock.order=15 //配置AOP代理顺序
cluster.lock.prefix-lock-key=TEST_CLUSTER_LOCK //配置锁键前缀
```

```
@ClusterLock("@testService.value+#user.name+#user.loves[1]+#max")
public String test(User user, int max) {
    System.out.println("service-test:" + Thread.currentThread().getName());
    return "test";
}
```

## @ClusterLock说明

1. value： SPEL表达式，可以使用@name引用SpringContext内的Bean对象，使用#name引用方法参数
2. key: 锁键区分，可以多个ClusterLock对应一个Key,暂时无用，后期可以进行锁策略处理
3. invalidTime： 锁自动释放时间(秒)
4. waitTime：等待锁时间(秒)， -1不等待

## 实现原理
1. Spring aop
2. Spring el Expression


## feature
1. 添加锁的可重入支持
2. 分布式锁的实现加入本地，zookeeper等支持，类似于SpringCache