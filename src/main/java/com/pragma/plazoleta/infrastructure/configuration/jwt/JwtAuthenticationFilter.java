package com.pragma.plazoleta.infrastructure.configuration.jwt;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int TOKEN_START_INDEX = 7;
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            extractToken(request)
                    .filter(this::isValidToken)
                    .ifPresent(token -> authenticateUserInContext(token, request));

        } catch (Exception e) {
            log.error("[JWT FILTER] Error inesperado estableciendo la autenticación en el contexto", e);
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(BEARER_PREFIX))
                .map(header -> header.substring(TOKEN_START_INDEX));
    }

    private boolean isValidToken(String token) {
        boolean valid = jwtTokenUtil.isTokenValid(token);
        if (!valid) {
            log.warn("[JWT FILTER] Token inválido o expirado");
        }
        return valid;
    }

    private void authenticateUserInContext(String token, HttpServletRequest request) {
        String email = jwtTokenUtil.extractEmail(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String role = jwtTokenUtil.extractRole(token);
            Long userId = jwtTokenUtil.extractUserId(token);

            log.debug("[JWT FILTER] Autenticando usuario: email={}, rol={}, id={}", email, role, userId);

            UsernamePasswordAuthenticationToken authToken = buildAuthenticationToken(userId, email, role);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    private UsernamePasswordAuthenticationToken buildAuthenticationToken(Long userId, String email, String role) {
        JwtUserDetails userDetails = new JwtUserDetails(userId, email, role);
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role));

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}