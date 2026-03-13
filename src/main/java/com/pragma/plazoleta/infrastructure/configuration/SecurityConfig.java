package com.pragma.plazoleta.infrastructure.configuration;

import com.pragma.plazoleta.infrastructure.configuration.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // Swagger
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Crear propietario: solo ADMINISTRADOR
                .antMatchers(HttpMethod.POST, "/user/").hasRole("ADMINISTRADOR")
                // Crear restaurante: solo ADMINISTRADOR
                .antMatchers(HttpMethod.POST, "/restaurant/").hasRole("ADMINISTRADOR")
                // Listar restaurantes: solo CLIENTE
                .antMatchers(HttpMethod.GET, "/restaurant/").hasRole("CLIENTE")
                // Listar platos de un restaurante: solo CLIENTE
                .antMatchers(HttpMethod.GET, "/plate/restaurant/**").hasRole("CLIENTE")
                // Crear y modificar platos: solo PROPIETARIO
                .antMatchers(HttpMethod.POST, "/plate/").hasRole("PROPIETARIO")
                .antMatchers(HttpMethod.PUT, "/plate/**").hasRole("PROPIETARIO")
                .antMatchers(HttpMethod.PATCH, "/plate/**/status").hasRole("PROPIETARIO")
                // Crear empleado: solo PROPIETARIO
                .antMatchers(HttpMethod.POST, "/employee/").hasRole("PROPIETARIO")
                // Crear pedido: solo CLIENTE
                .antMatchers(HttpMethod.POST, "/order/").hasRole("CLIENTE")
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated();

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

