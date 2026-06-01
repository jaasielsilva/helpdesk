package com.jaasielsilva.helpdesk.ratelimit;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

/**
 * Rate limiter para o endpoint de login.
 * Permite no máximo 5 tentativas por minuto por IP.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_TENTATIVAS = 5;
    private static final Duration JANELA = Duration.ofMinutes(1);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String clientIp) {
        Bucket bucket = buckets.computeIfAbsent(clientIp, this::novoBucket);
        return bucket.tryConsume(1);
    }

    private Bucket novoBucket(String ip) {
        Bandwidth limite = Bandwidth.builder()
                .capacity(MAX_TENTATIVAS)
                .refillGreedy(MAX_TENTATIVAS, JANELA)
                .build();
        return Bucket.builder().addLimit(limite).build();
    }
}
