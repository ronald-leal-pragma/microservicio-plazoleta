package com.pragma.plazoleta.infrastructure.configuration.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtUserDetails {
    private final Long id;
    private final String email;
    private final String rol;
}
