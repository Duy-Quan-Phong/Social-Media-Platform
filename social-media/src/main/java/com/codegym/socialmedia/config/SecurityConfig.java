package com.codegym.socialmedia.config;

import com.codegym.socialmedia.component.CustomAuthFailureHandler;
import com.codegym.socialmedia.service.user.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Autowired
    private CustomOAuth2UserService oauth2UserService;

    @Autowired
    private CustomAuthFailureHandler customAuthFailureHandler;

    @Bean
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        // XSRF-TOKEN cookie readable by JS so fetch() can include the token
        CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();

        http
                .securityMatcher("/**")
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("img-src 'self' https://lh3.googleusercontent.com https://*.fbcdn.net https://res.cloudinary.com https://graph.facebook.com https://i.imgur.com https://secure.gravatar.com data: blob:;")
                        )
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(org.springframework.security.config.Customizer.withDefaults())
                        .xssProtection(xss -> xss.headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepo)
                        .csrfTokenRequestHandler(requestHandler)
                        // SockJS performs HTTP upgrade — exempt the WS endpoint
                        .ignoringRequestMatchers("/ws/**", "/ws/info/**")
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**",
                                "/api/debug/**", "/api/check/**", "/ws/**", "/forgot-password", "/reset-password",
                                "/search", "/hashtag/**", "/webjars/**",
                                "/api/search/**", "/api/hashtags/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/news-feed", true)
                        .failureHandler(customAuthFailureHandler)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
                        .successHandler((request, response, authentication) -> response.sendRedirect("/news-feed"))
                        .failureHandler(customAuthFailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}
