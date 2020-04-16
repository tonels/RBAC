package com.dxj.monitor.controller;

import com.dxj.common.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 接口限流测试类
 *
 * @author dxj
 * @date 2019-04-24
 */
@RestController
@RequestMapping("api")
public class LimitController {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

    /**
     * 测试限流注解，下面配置说明该接口 60秒内最多只能访问 10次，保存到redis的键名为 limit_test，
     */
    @RateLimit(key = "test", period = 60, count = 10, name = "testLimit", prefix = "annotation")
    @GetMapping("/limit")
    public int testLimit() {
        return ATOMIC_INTEGER.incrementAndGet();
    }
}
