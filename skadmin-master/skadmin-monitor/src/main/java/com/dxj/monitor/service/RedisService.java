package com.dxj.monitor.service;

import com.dxj.common.util.PageUtil;
import com.dxj.monitor.domain.vo.RedisVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

/**
 * 可自行扩展
 *
 * @author dxj
 * @date 2019-04-10
 */
@Service
public class RedisService {

    private final JedisPool pool;

    @Autowired
    public RedisService(JedisPool pool) {
        this.pool = pool;
    }

    public Page findByKey(String key, Pageable pageable) {
        try (Jedis jedis = pool.getResource()) {
            List<RedisVo> redisVos = new ArrayList<>();

            if (!key.equals("*")) {
                key = "*" + key + "*";
            }
            for (String s : jedis.keys(key)) {
                RedisVo redisVo = new RedisVo(s, jedis.get(s));
                redisVos.add(redisVo);
            }
            return new PageImpl<>(
                    PageUtil.toPage(pageable.getPageNumber(), pageable.getPageSize(), redisVos),
                    pageable, redisVos.size());
        }

    }

    public void save(RedisVo redisVo) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(redisVo.getKey(), redisVo.getValue());
        }
    }

    public void delete(String key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
        }

    }

    public void flushDb() {
        try (Jedis jedis = pool.getResource()) {
            jedis.flushAll();
        }
        // 释放资源还给连接池

    }
}
