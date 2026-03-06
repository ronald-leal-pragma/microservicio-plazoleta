package com.pragma.plazoleta.infrastructure.out.jpa.adapter;

import com.pragma.plazoleta.domain.model.RolModel;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.infrastructure.exception.UserAlreadyExistsException;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.RolEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.UserEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IUserEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IRolRepository;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserJpaAdapterTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IRolRepository rolRepository;

    @Mock
    private IUserEntityMapper userEntityMapper;

    @InjectMocks
    private UserJpaAdapter userJpaAdapter;

    private UserModel userModel;

    @BeforeEach
    void setUp() {
        RolModel rolModel = new RolModel(2L, "PROPIETARIO", "Rol de propietario de restaurante");
        userModel = new UserModel(
                null, "Juan", "Pérez", "1234567890",
                "+573005698325", LocalDate.of(1990, 5, 15),
                "juan@email.com", "encodedPassword", rolModel
        );
    }

    @Test
    @DisplayName("Debe guardar el usuario exitosamente cuando no hay correo ni documento duplicado")
    void saveUser_shouldSaveUser_whenNoConflictsExist() {
        when(userRepository.existsByCorreo("juan@email.com")).thenReturn(false);
        when(userRepository.existsByDocumentoDeIdentidad("1234567890")).thenReturn(false);
        RolEntity rolEntity = new RolEntity(2L, "PROPIETARIO", "Rol de propietario");
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolEntity));
        UserEntity userEntity = new UserEntity();
        when(userEntityMapper.toEntity(userModel)).thenReturn(userEntity);

        assertDoesNotThrow(() -> userJpaAdapter.saveUser(userModel));

        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    @DisplayName("Debe lanzar UserAlreadyExistsException cuando el correo ya está registrado")
    void saveUser_shouldThrowUserAlreadyExistsException_whenEmailAlreadyExists() {
        when(userRepository.existsByCorreo("juan@email.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userJpaAdapter.saveUser(userModel));

        verify(userRepository, never()).save(any());
        verify(rolRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Debe lanzar UserAlreadyExistsException cuando el documento ya está registrado")
    void saveUser_shouldThrowUserAlreadyExistsException_whenDocumentAlreadyExists() {
        when(userRepository.existsByCorreo(anyString())).thenReturn(false);
        when(userRepository.existsByDocumentoDeIdentidad("1234567890")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userJpaAdapter.saveUser(userModel));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe crear y persistir el rol cuando no existe en base de datos")
    void saveUser_shouldCreateAndSaveRol_whenRolDoesNotExistInDb() {
        when(userRepository.existsByCorreo(anyString())).thenReturn(false);
        when(userRepository.existsByDocumentoDeIdentidad(anyString())).thenReturn(false);
        when(rolRepository.findById(2L)).thenReturn(Optional.empty());
        RolEntity savedRol = new RolEntity(2L, "PROPIETARIO", "Rol de propietario");
        when(rolRepository.save(any(RolEntity.class))).thenReturn(savedRol);
        UserEntity userEntity = new UserEntity();
        when(userEntityMapper.toEntity(userModel)).thenReturn(userEntity);

        assertDoesNotThrow(() -> userJpaAdapter.saveUser(userModel));

        verify(rolRepository, times(1)).save(any(RolEntity.class));
        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    @DisplayName("No debe persistir el rol cuando ya existe en base de datos")
    void saveUser_shouldNotSaveRol_whenRolAlreadyExistsInDb() {
        when(userRepository.existsByCorreo(anyString())).thenReturn(false);
        when(userRepository.existsByDocumentoDeIdentidad(anyString())).thenReturn(false);
        RolEntity existingRol = new RolEntity(2L, "PROPIETARIO", "Rol de propietario");
        when(rolRepository.findById(2L)).thenReturn(Optional.of(existingRol));
        UserEntity userEntity = new UserEntity();
        when(userEntityMapper.toEntity(userModel)).thenReturn(userEntity);

        userJpaAdapter.saveUser(userModel);

        verify(rolRepository, never()).save(any(RolEntity.class));
        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    @DisplayName("Debe asignar el rolEntity al userEntity antes de guardar")
    void saveUser_shouldSetRolEntityOnUserEntity_beforeSaving() {
        when(userRepository.existsByCorreo(anyString())).thenReturn(false);
        when(userRepository.existsByDocumentoDeIdentidad(anyString())).thenReturn(false);
        RolEntity rolEntity = new RolEntity(2L, "PROPIETARIO", "Rol de propietario");
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolEntity));
        UserEntity userEntity = new UserEntity();
        when(userEntityMapper.toEntity(userModel)).thenReturn(userEntity);

        userJpaAdapter.saveUser(userModel);

        assertEquals(rolEntity, userEntity.getRol());
    }
}
