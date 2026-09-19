package com.markethub.product;
import com.markethub.auth.*;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
public class PublicProductSecurityConfig {
 @Bean @Order(0)
 SecurityFilterChain publicProductSecurity(HttpSecurity http,JwtAuthenticationFilter jwt,RestAuthenticationEntryPoint entry) throws Exception {
  return http.securityMatcher("/api/products/**").csrf(c->c.disable()).cors(cors -> {}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .exceptionHandling(e->e.authenticationEntryPoint(entry)).authorizeHttpRequests(a->a
    .requestMatchers(HttpMethod.GET,"/api/products/**").permitAll()
    .requestMatchers(HttpMethod.POST,"/api/products/*/reviews").hasRole("CUSTOMER")
    .requestMatchers(HttpMethod.PUT,"/api/products/*/reviews/me").hasRole("CUSTOMER")
    .requestMatchers(HttpMethod.DELETE,"/api/products/*/reviews/me").hasRole("CUSTOMER")
    .anyRequest().authenticated()).addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();
 }
}