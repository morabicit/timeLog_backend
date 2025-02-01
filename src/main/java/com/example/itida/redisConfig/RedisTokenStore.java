package com.example.itida.redisConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenStore {

    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    private final RedisTemplate<String, String> redisTemplate;

    public RedisTokenStore(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeAccessToken(String token, String email) {
        String key = ACCESS_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, email, 10, TimeUnit.MINUTES);
    }

    public void storeRefreshToken(String token, String email) {
        String key = REFRESH_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, email, 30, TimeUnit.MINUTES);
    }
    public void deleteAccessToken(String token) {
        String key = ACCESS_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
    }

    public void deleteRefreshToken(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
    }

    public boolean isAccessTokenExists(String token) {
        String key = ACCESS_TOKEN_PREFIX + token;
        return redisTemplate.hasKey(key);
    }

    public boolean isRefreshTokenExists(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        return redisTemplate.hasKey(key);
    }
}
