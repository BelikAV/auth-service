package com.example.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh_token:";
    private static final long TTL_DAYS = 30;

    private final StringRedisTemplate redisTemplate;


    public String createRefreshToken(String username) {
        String token = UUID.randomUUID().toString();
        String key = KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, username, TTL_DAYS, TimeUnit.DAYS);
        log.info("Создан refresh token для пользователя: {}", username);
        return token;
    }

    public Optional<String> getUsernameByToken(String token) {
        String key = KEY_PREFIX + token;
        String username = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(username);
    }

    public void deleteRefreshToken(String token) {
        String key = KEY_PREFIX + token;
        Boolean deleted = redisTemplate.delete(key);
        log.info("Удалён refresh token: {}, результат: {}", token, deleted);
    }

}
