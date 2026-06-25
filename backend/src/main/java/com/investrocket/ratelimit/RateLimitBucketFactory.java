package com.investrocket.ratelimit;

import java.time.Duration;

import io.github.bucket4j.Bucket;

final class RateLimitBucketFactory {

    private RateLimitBucketFactory() {
    }

    static Bucket perMinute(int requestsPerMinute) {
        int capacity = Math.max(1, requestsPerMinute);
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(capacity)
                        .refillIntervally(capacity, Duration.ofMinutes(1)))
                .build();
    }
}
