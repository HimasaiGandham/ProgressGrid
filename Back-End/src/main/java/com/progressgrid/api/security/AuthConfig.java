package com.progressgrid.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

/**
 * Requires a valid session token on every habit endpoint and exposes the caller's id to
 * controllers as a request attribute. Controllers must read the id from here, never from
 * anything the client sends.
 */
@Configuration
public class AuthConfig implements WebMvcConfigurer {

    /** Request attribute holding the caller's user id, set only after their token checks out. */
    public static final String USER_ID = "authenticatedUserId";

    private final TokenService tokenService;

    public AuthConfig(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws IOException {
                if (CorsUtils.isPreFlightRequest(request)) {
                    return true;
                }
                String header = request.getHeader(HttpHeaders.AUTHORIZATION);
                Long userId = (header != null && header.startsWith("Bearer "))
                        ? tokenService.userIdFrom(header.substring(7))
                        : null;
                if (userId == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"message\":\"Please sign in again.\"}");
                    return false;
                }
                request.setAttribute(USER_ID, userId);
                return true;
            }
        }).addPathPatterns("/api/habits", "/api/habits/**");
    }
}
