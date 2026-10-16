package com.evidlock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS configuration allowing Vercel-hosted frontends and localhost dev.
 * Individual @CrossOrigin annotations on controllers are kept for backward-compat
 * but this global config takes precedence for production origins.
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
                String[] origins;
                if (allowedOrigins != null && !allowedOrigins.isBlank()) {
                    origins = allowedOrigins.split(",");
                } else {
                    origins = new String[]{
                        "http://localhost:4200",
                        "http://127.0.0.1:4200",
                        "http://localhost:8080",
                        "http://127.0.0.1:8080",
                        "http://localhost:8081"
                    };
                }
                registry.addMapping("/**")
                        .allowedOrigins(origins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
