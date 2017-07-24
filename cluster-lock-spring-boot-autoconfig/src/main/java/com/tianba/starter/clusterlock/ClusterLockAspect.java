package com.tianba.starter.clusterlock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

@Aspect
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnBean(StringRedisTemplate.class)
@EnableConfigurationProperties(ClusterLockProperties.class)
public class ClusterLockAspect implements Ordered, ApplicationContextAware {
    private final static Logger LOGGER = LoggerFactory.getLogger(ClusterLockAspect.class);

    @Autowired
    private ClusterLockProperties clusterLockProperties;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private SpelParserConfiguration config;

    private ApplicationContext applicationContext;

    private BeanFactoryResolver beanFactoryResolver;

    public ClusterLockAspect() {
        config = new SpelParserConfiguration(SpelCompilerMode.MIXED,
                this.getClass().getClassLoader(), true, true, 100);
    }

    @Pointcut("@annotation(com.tianba.starter.clusterlock.ClusterLock)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        final StringBuilder keyBuilder = new StringBuilder(clusterLockProperties.getPrefixLockKey());
        Object[] ars = joinPoint.getArgs();

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        ClusterLock lock = method.getAnnotation(ClusterLock.class);
        final String key = lock.key();
        if (StringUtils.isEmpty(key)) {
            keyBuilder.append(method.getName());
        } else {
            keyBuilder.append(key);
        }

        final String elValue = lock.value();
        if (!StringUtils.isEmpty(elValue)) {
            SpelExpressionParser spelExpressionParser = new SpelExpressionParser(config);
            StandardEvaluationContext context = new StandardEvaluationContext(applicationContext);
            String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(method);
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], ars[i]);
            }
            context.setBeanResolver(beanFactoryResolver);
            Expression expr = spelExpressionParser.parseExpression(elValue);
            String value = expr.getValue(context, String.class);
            keyBuilder.append(value);
        }

        long liveTime = lock.invalidTime();
        long waitTime = lock.waitTime() * 1000;
        boolean locked = false;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(method.getName() + " lock: " + keyBuilder.toString());
            }
            locked = lockWait(keyBuilder.toString(), waitTime, liveTime);
            if (locked) {
                return joinPoint.proceed();
            } else {
                throw new LockException("操作过于频繁，请稍后重试");
            }
        } finally {
            if (locked) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(method.getName() + " finally unlock: " + keyBuilder.toString());
                }
                redisTemplate.execute(new RedisCallback<Boolean>() {
                    @Override
                    public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                        long result = connection.del(keyBuilder.toString().getBytes(Charset.forName("UTF-8")));
                        return result == 1;
                    }
                });
            }
        }
    }

    @Override
    public int getOrder() {
        return clusterLockProperties.getOrder();
    }

    private boolean lockWait(String key, long wait, long expire) {
        Long totalWait = 0L;
        boolean isOk;
        long interval = 1000;//时间间隔500ms
        while (true) {
            isOk = lockWithExpire(key, expire);
            if (isOk || totalWait > wait) {
                break;
            }
            try {
                Thread.sleep(interval);
                totalWait += interval;
            } catch (InterruptedException e) {
                break;
            }
        }
        return isOk;
    }

    private boolean lockWithExpire(String key, final Long expire) {
        final byte[] keyByte = key.getBytes(Charset.forName("UTF-8"));
        Long result = redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                Long val = connection.incr(keyByte);
                if (expire > 0) {
                    connection.expire(keyByte, expire);
                }
                return val;
            }
        });
        return result == 1;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.beanFactoryResolver = new BeanFactoryResolver(applicationContext);
    }
}
