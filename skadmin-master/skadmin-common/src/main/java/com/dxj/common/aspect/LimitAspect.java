package com.dxj.common.aspect;

import com.dxj.common.annotation.RateLimit;
import com.dxj.common.util.IpInfoUtil;
import com.dxj.common.util.RequestHolder;
import com.dxj.common.util.StringUtil;
import com.google.common.collect.ImmutableList;
import com.dxj.common.exception.BadRequestException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author dxj
 * @date 2019-4-1
 */
@Aspect
@Component
public class LimitAspect {

    private final RedisTemplate redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(LimitAspect.class);

    @Autowired
    public LimitAspect(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Pointcut("@annotation(com.dxj.common.annotation.RateLimit)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = RequestHolder.getHttpServletRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method signatureMethod = signature.getMethod();
        RateLimit limit = signatureMethod.getAnnotation(RateLimit.class);
        LimitType limitType = limit.limitType();
        String key = limit.key();
        if (StringUtil.isEmpty(key)) {
            if (limitType == LimitType.IP) {
                key = IpInfoUtil.getIpAddr(request);
            } else {
                key = signatureMethod.getName();
            }
        }

        ImmutableList keys = ImmutableList.of(StringUtil.join(limit.prefix(), "_", key, "_", request.getRequestURI().replaceAll("/", "_")));

        String luaScript = buildLuaScript();
        RedisScript<Number> redisScript = new DefaultRedisScript<>(luaScript, Number.class);
        Number count = (Number) redisTemplate.execute(redisScript, keys, limit.count(), limit.period());
        if (null != count && count.intValue() <= limit.count()) {
            logger.info("第{}次访问key为 {}，描述为 [{}] 的接口", count, keys, limit.name());
            return joinPoint.proceed();
        } else {
            throw new BadRequestException("访问次数受限制");
        }
    }

    /**
     * 限流脚本
     */
    private String buildLuaScript() {
        return "local c" +
                "\nc = redis.call('get',KEYS[1])" +
                "\nif c and tonumber(c) > tonumber(ARGV[1]) then" +
                "\nreturn c;" +
                "\nend" +
                "\nc = redis.call('incr',KEYS[1])" +
                "\nif tonumber(c) == 1 then" +
                "\nredis.call('expire',KEYS[1],ARGV[2])" +
                "\nend" +
                "\nreturn c;";
    }
}
