package com.example.demo.config.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_USER_ATTRIBUTE = "authUser";

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isPublicRequest(request, path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        JwtUtil.JwtClaims claims = token == null ? null : jwtUtil.validateToken(token);
        if (claims == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Missing or invalid JWT token");
            return;
        }

        User user = userRepository.findById(claims.userId()).orElse(null);
        if (user == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Authenticated user no longer exists");
            return;
        }

        request.setAttribute(AUTH_USER_ATTRIBUTE, user);
        filterChain.doFilter(request, response);
    }

    private boolean isPublicRequest(HttpServletRequest request, String path) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/api/auth/")
                || path.equals("/")
                || path.startsWith("/error");
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring("Bearer ".length()).trim();
        }

        // EventSource cannot set custom Authorization headers, so SSE passes token in query string.
        String queryToken = request.getParameter("token");
        return queryToken == null || queryToken.isBlank() ? null : queryToken.trim();
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
