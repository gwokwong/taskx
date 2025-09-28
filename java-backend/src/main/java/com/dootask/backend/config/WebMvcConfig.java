package com.dootask.backend.config;

import lombok.RequiredArgsConstructor;
// Temporarily disabled
// import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    // Temporarily disabled
    // private final LoggingInterceptor loggingInterceptor;

    /*
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/system/settings/public"
                );
    }
    */
}