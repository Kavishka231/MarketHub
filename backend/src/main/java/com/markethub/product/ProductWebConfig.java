package com.markethub.product;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ProductWebConfig implements WebMvcConfigurer {
    private final ProductRequestInterceptor interceptor;
    public ProductWebConfig(ProductRequestInterceptor interceptor) { this.interceptor = interceptor; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/api/products", "/api/vendor/products");
    }
}
