package middleware

import (
    "net/http"
    "sync"
    "time"
)

// TokenBucket is a simple leaky bucket rate limiter.
type TokenBucket struct {
    capacity     int
    tokens       float64
    fillRatePerS float64
    lastRefill   time.Time
    mu           sync.Mutex
}

func NewTokenBucket(capacity int, fillPerSecond float64) *TokenBucket {
    return &TokenBucket{
        capacity:     capacity,
        tokens:       float64(capacity),
        fillRatePerS: fillPerSecond,
        lastRefill:   time.Now(),
    }
}

func (b *TokenBucket) allow() bool {
    b.mu.Lock()
    defer b.mu.Unlock()
    now := time.Now()
    elapsed := now.Sub(b.lastRefill).Seconds()
    b.tokens += elapsed * b.fillRatePerS
    if b.tokens > float64(b.capacity) {
        b.tokens = float64(b.capacity)
    }
    b.lastRefill = now
    if b.tokens >= 1 {
        b.tokens -= 1
        return true
    }
    return false
}

// RateLimit wraps an http.Handler with global token bucket rate limiting.
func RateLimit(next http.Handler, bucket *TokenBucket) http.Handler {
    return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if !bucket.allow() {
            w.WriteHeader(http.StatusTooManyRequests)
            _, _ = w.Write([]byte("rate limit exceeded"))
            return
        }
        next.ServeHTTP(w, r)
    })
}


