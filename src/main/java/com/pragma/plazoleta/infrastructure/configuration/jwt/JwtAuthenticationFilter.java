package com.pragma.plazoleta.infrastructure.configuration.jwt;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtTokenUtil.isTokenValid(token)) {
            log.warn("[JWT FILTER] Token inválido o expirado");
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtTokenUtil.extractEmail(token);
        String rol = jwtTokenUtil.extractRole(token);
        Long userId = jwtTokenUtil.extractUserId(token);

        log.debug("[JWT FILTER] Token válido: email={}, rol={}, id={}", email, rol, userId);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String roleWithPrefix = "ROLE_" + rol;
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    new JwtUserDetails(userId, email, rol),
                    null,
                    List.of(new SimpleGrantedAuthority(roleWithPrefix))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
