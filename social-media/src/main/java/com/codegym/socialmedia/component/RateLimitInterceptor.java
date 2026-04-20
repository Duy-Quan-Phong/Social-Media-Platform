package com.codegym.socialmedia.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // Limits per endpoint pattern per IP per minute
    private static final Map<String, Integer> LIMITS = Map.of(
            "/api/comments/add",   20,
            "/posts/api/create",   10,
            "/posts/api/like/",    30,
            "/posts/api/react/",   30,
            "/forgot-password",     5
    );

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();
        String ip  = getClientIp(request);

        for (Map.Entry<String, Integer> entry : LIMITS.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                String key = entry.getKey() + ":" + ip;
                if (!rateLimitService.isAllowed(key, entry.getValue())) {
                    response.setStatus(429);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                            "success", false,
                            "message", "Bạn thao tác quá nhanh, vui lòng thử lại sau."
                    )));
                    return false;
                }
                break;
            }
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
