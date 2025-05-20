package com.netdatel.documentserviceapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Configuration
public class LoggingConfig {

    @Bean
    public LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilterRegistration(LoggingFilter loggingFilter) {
        FilterRegistrationBean<LoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(loggingFilter);
        registration.addUrlPatterns("/api/*");
        registration.setName("loggingFilter");
        registration.setOrder(1);
        return registration;
    }

    @Component
    @Slf4j
    public static class LoggingFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            long startTime = System.currentTimeMillis();

            // Log de la solicitud
            String requestMethod = request.getMethod();
            String requestUrl = request.getRequestURI();
            String queryString = request.getQueryString();
            String fullUrl = queryString != null ? requestUrl + "?" + queryString : requestUrl;
            String clientIp = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");

            log.info("Request: {} {} from IP: {}, User-Agent: {}", requestMethod, fullUrl, clientIp, userAgent);

            // Envolver la respuesta para capturar el código de estado
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

            try {
                // Continuar con la cadena de filtros
                filterChain.doFilter(request, responseWrapper);
            } finally {
                // Log de la respuesta
                long duration = System.currentTimeMillis() - startTime;
                int status = responseWrapper.getStatus();

                log.info("Response: {} {} - {} in {}ms", requestMethod, fullUrl, status, duration);

                // Importante: copiar el contenido de la respuesta al cliente
                responseWrapper.copyBodyToResponse();
            }
        }

        private String getClientIp(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return request.getRemoteAddr();
        }
    }
}