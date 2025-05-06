package com.quodbiometria.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final CacheManager cacheManager;

    @Value("${app.security.rate-limit.login-attempts:5}")
    private int loginAttemptsLimit;

    @Value("${app.security.rate-limit.login-duration:60}")
    private int loginDurationInSeconds;

    public boolean tryConsume(String key) {
        return getBucket(key).tryConsume(1);
    }

    @SuppressWarnings("unused")
    public long getAvailableTokens(String key) {
        return getBucket(key).getAvailableTokens();
    }

    private Bucket getBucket(String key) {
        org.springframework.cache.Cache cache = cacheManager.getCache("rateLimitBuckets");
        if (cache != null) {
            Bucket bucket = cache.get(key, Bucket.class);
            if (bucket == null) {
                bucket = createNewBucket(key);
                cache.put(key, bucket);
            }
            return bucket;
        }

        return createNewBucket(key);
    }

    @SuppressWarnings("unused")
    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.classic(loginAttemptsLimit,
                Refill.intervally(loginAttemptsLimit, Duration.ofSeconds(loginDurationInSeconds)));
        return Bucket.builder().addLimit(limit).build();
    }
}