package com.ledgerflow;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.function.Supplier;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;
import org.springframework.util.StringUtils;

@Configuration @EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean UserDetailsService users(UserRepository repository) {
        return email -> repository.findByEmail(email.toLowerCase().trim())
            .map(u -> User.withUsername(u.email).password(u.passwordHash).roles(u.role.name()).build())
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }
    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http
            .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new SpaCsrfHandler()))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.GET, "/api/auth/me", "/api/auth/csrf", "/actuator/health").permitAll()
                .requestMatchers("/api/auth/login").permitAll().anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((q, s, x) -> json(s, 401, "Sign in to continue"))
                .accessDeniedHandler((q, s, x) -> json(s, 403, "Permission denied or CSRF token missing; refresh and sign in again")))
            .formLogin(f -> f.loginProcessingUrl("/api/auth/login")
                .successHandler((q, s, a) -> json(s, 200, "Signed in"))
                .failureHandler((q, s, x) -> json(s, 401, "Incorrect email or password")))
            .logout(l -> l.logoutUrl("/api/auth/logout").logoutSuccessHandler((q, s, a) -> json(s, 200, "Signed out")))
            .build();
    }
    private static void json(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status); response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
    // Cookie/header tokens are plain; request attributes retain BREACH protection.
    static final class SpaCsrfHandler implements CsrfTokenRequestHandler {
        private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
        private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> token) {
            xor.handle(request, response, token); token.get();
        }
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken token) {
            return (StringUtils.hasText(request.getHeader(token.getHeaderName())) ? plain : xor)
                .resolveCsrfTokenValue(request, token);
        }
    }
}
