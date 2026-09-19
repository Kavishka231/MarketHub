package com.markethub.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markethub.common.ApiErrorResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Component
public class DistributedRateLimitInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedRateLimitInterceptor.class);
    private static final DefaultRedisScript<Long> INCREMENT_WINDOW = new DefaultRedisScript<>(
            "local n=redis.call('INCR',KEYS[1]); "
                    + "if n==1 then redis.call('EXPIRE',KEYS[1],ARGV[1]) end; return n",
            Long.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final Counter rejectedCounter;
    private final Counter unavailableCounter;

    public DistributedRateLimitInterceptor(
            StringRedisTemplate redis,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry,
            @Value("${rate-limit.enabled:true}") boolean enabled) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.rejectedCounter = Counter.builder("markethub.rate_limit.rejections")
                .description("Requests rejected by distributed rate limiting")
                .register(meterRegistry);
        this.unavailableCounter = Counter.builder("markethub.rate_limit.redis_failures")
                .description("Rate-limit checks that could not reach Redis")
                .register(meterRegistry);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!enabled) {
            return true;
        }
        Policy policy = Policy.forRequest(request);
        if (policy == null) {
            return true;
        }

        String identity = identity(request);
        long window = Instant.now().getEpochSecond() / policy.windowSeconds();
        String key = "markethub:rate:" + policy.category() + ":" + digest(identity) + ":" + window;

        try {
            Long count = redis.execute(
                    INCREMENT_WINDOW,
                    List.of(key),
                    Long.toString(policy.windowSeconds() + 1));
            if (count != null && count > policy.limit()) {
                rejectedCounter.increment();
                writeRejection(request, response, policy.windowSeconds());
                return false;
            }
            return true;
        } catch (RedisConnectionFailureException exception) {
            unavailableCounter.increment();
            LOGGER.warn("rate_limit_unavailable category={} failClosed={}", policy.category(), policy.failClosed());
            if (policy.failClosed()) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(
                        Instant.now(),
                        503,
                        "Service Unavailable",
                        "RATE_LIMIT_UNAVAILABLE",
                        "Authentication protection is temporarily unavailable",
                        request.getRequestURI(),
                        requestId(request),
                        null));
                return false;
            }
            return true;
        }
    }

    private void writeRejection(HttpServletRequest request, HttpServletResponse response, int retryAfter)
            throws Exception {
        response.setStatus(429);
        response.setHeader("Retry-After", Integer.toString(retryAfter));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(
                Instant.now(),
                429,
                "Too Many Requests",
                "RATE_LIMIT_EXCEEDED",
                "Too many requests. Please try again later.",
                request.getRequestURI(),
                requestId(request),
                Map.of("retryAfterSeconds", Integer.toString(retryAfter))));
    }

    private String identity(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        String address = forwarded == null ? request.getRemoteAddr() : forwarded.split(",", 2)[0].trim();
        return "ip:" + address;
    }

    private String requestId(HttpServletRequest request) {
        Object value = request.getAttribute("requestId");
        return value == null ? null : value.toString();
    }

    private String digest(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    record Policy(String category, int limit, int windowSeconds, boolean failClosed) {
        static Policy forRequest(HttpServletRequest request) {
            String method = request.getMethod();
            String path = request.getRequestURI();
            if ("POST".equals(method) && path.matches("/api/auth/(login|register)")) {
                return new Policy("auth", 10, 60, true);
            }
            if ("POST".equals(method) && "/api/orders/checkout".equals(path)) {
                return new Policy("checkout", 5, 60, false);
            }
            if (("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))
                    && path.matches("/api/products/[^/]+/reviews(/me)?")) {
                return new Policy("review", 10, 60, false);
            }
            if (!"GET".equals(method) && (path.startsWith("/api/admin/") || path.startsWith("/api/vendor/"))) {
                return new Policy(path.startsWith("/api/admin/") ? "admin" : "vendor", 30, 60, false);
            }
            return null;
        }
    }
}