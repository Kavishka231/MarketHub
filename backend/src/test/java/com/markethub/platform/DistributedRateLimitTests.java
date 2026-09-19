package com.markethub.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markethub.ratelimit.DistributedRateLimitInterceptor;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DistributedRateLimitTests {

    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final SimpleMeterRegistry metrics = new SimpleMeterRegistry();
    private final DistributedRateLimitInterceptor interceptor =
            new DistributedRateLimitInterceptor(redis, new ObjectMapper().findAndRegisterModules(), metrics, true);

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsRequestsBelowLimitAndRejectsExceededAuthRequests() throws Exception {
        when(redis.execute(any(), anyList(), anyString())).thenReturn(1L, 11L);
        MockHttpServletRequest request = request("POST", "/api/auth/login");

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();

        MockHttpServletResponse rejected = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(request, rejected, new Object())).isFalse();
        assertThat(rejected.getStatus()).isEqualTo(429);
        assertThat(rejected.getHeader("Retry-After")).isEqualTo("60");
        assertThat(rejected.getContentAsString()).contains("RATE_LIMIT_EXCEEDED");
    }

    @Test
    void authenticatedMutationUsesRateLimitWithoutExposingIdentity() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("customer@example.com", null, java.util.List.of()));
        when(redis.execute(any(), anyList(), anyString())).thenReturn(6L);

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean allowed = interceptor.preHandle(
                request("POST", "/api/orders/checkout"), response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getContentAsString()).doesNotContain("customer@example.com");
    }

    @Test
    void failsClosedForAuthAndOpenForBusinessWhenRedisIsUnavailable() throws Exception {
        when(redis.execute(any(), anyList(), anyString()))
                .thenThrow(new RedisConnectionFailureException("offline"));

        MockHttpServletResponse authResponse = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(
                request("POST", "/api/auth/register"), authResponse, new Object())).isFalse();
        assertThat(authResponse.getStatus()).isEqualTo(503);

        MockHttpServletResponse checkoutResponse = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(
                request("POST", "/api/orders/checkout"), checkoutResponse, new Object())).isTrue();
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr("192.0.2.10");
        request.setAttribute("requestId", "rate-test-request");
        return request;
    }
}