package com.codegym.socialmedia.component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "Sai tên đăng nhập hoặc mật khẩu.";

        if (exception instanceof BadCredentialsException) {
            errorMessage = "Mật khẩu không chính xác.";
        } else if (exception.getMessage() != null) {
            errorMessage = exception.getMessage();
        }

        // Chuyển hướng kèm message
        request.getSession().setAttribute("error_message", errorMessage);
        response.sendRedirect("/login?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));

    }
}

