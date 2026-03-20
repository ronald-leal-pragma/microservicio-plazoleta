package com.pragma.plazoleta.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Lista cerrada de roles del sistema.
 * Los valores son fijos y no deben modificarse sin coordinación con el microservicio de usuarios.
 */
@Getter
@RequiredArgsConstructor
public enum RoleType {
    ADMIN(1L, "ADMIN", "Administrador del sistema"),
    PROPIETARIO(2L, "PROPIETARIO", "Propietario de restaurante"),
    EMPLEADO(4L, "EMPLEADO", "Empleado de restaurante"),
    CLIENTE(3L, "CLIENTE", "Cliente del sistema");

    private final Long id;
    private final String nombre;
    private final String descripcion;

    /**
     * Busca un RoleType por su nombre.
     *
     * @param nombre nombre del rol
     * @return Optional con el RoleType si existe
     */
    public static Optional<RoleType> fromNombre(String nombre) {
        return Arrays.stream(values())
                .filter(role -> role.nombre.equalsIgnoreCase(nombre))
                .findFirst();
    }

    /**
     * Busca un RoleType por su ID.
     *
     * @param id ID del rol
     * @return Optional con el RoleType si existe
     */
    public static Optional<RoleType> fromId(Long id) {
        return Arrays.stream(values())
                .filter(role -> role.id.equals(id))
                .findFirst();
    }

    /**
     * Convierte este RoleType a un RolModel.
     *
     * @return RolModel con los datos de este rol
     */
    public RolModel toModel() {
        return RolModel.builder()
                .id(this.id)
                .nombre(this.nombre)
                .descripcion(this.descripcion)
                .build();
    }
}
