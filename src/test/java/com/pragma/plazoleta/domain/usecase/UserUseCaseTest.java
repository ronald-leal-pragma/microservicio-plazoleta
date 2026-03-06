package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.ExceptionConstants;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.domain.spi.IPasswordEncoderPort;
import com.pragma.plazoleta.domain.spi.IUserPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock
    private IUserPersistencePort userPersistencePort;

    @Mock
    private IPasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private UserUseCase userUseCase;

    private UserModel validUser;

    @BeforeEach
    void setUp() {
        validUser = new UserModel();
        validUser.setNombre("Juan");
        validUser.setApellido("Pérez");
        validUser.setDocumentoDeIdentidad("1234567890");
        validUser.setCelular("+573005698325");
        validUser.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        validUser.setCorreo("juan@email.com");
        validUser.setClave("MiClave123");
    }

    @Test
    @DisplayName("Debe encriptar la contraseña cuando el usuario es mayor de edad")
    void saveUser_shouldEncodePassword_whenUserIsAdult() {
        when(passwordEncoderPort.encode("MiClave123")).thenReturn("encodedPassword");

        userUseCase.saveUser(validUser);

        assertEquals("encodedPassword", validUser.getClave());
    }

    @Test
    @DisplayName("Debe asignar el rol PROPIETARIO cuando el usuario es mayor de edad")
    void saveUser_shouldAssignRolPropietario_whenUserIsAdult() {
        when(passwordEncoderPort.encode(anyString())).thenReturn("encoded");

        userUseCase.saveUser(validUser);

        assertNotNull(validUser.getRol());
        assertEquals(ExceptionConstants.ROL_PROPIETARIO_ID, validUser.getRol().getId());
        assertEquals(ExceptionConstants.ROL_PROPIETARIO, validUser.getRol().getNombre());
    }

    @Test
    @DisplayName("Debe invocar persistencia una vez cuando el usuario es válido")
    void saveUser_shouldCallPersistencePortOnce_whenUserIsValid() {
        when(passwordEncoderPort.encode(anyString())).thenReturn("hashed");

        userUseCase.saveUser(validUser);

        verify(userPersistencePort, times(1)).saveUser(validUser);
    }

    @Test
    @DisplayName("Debe lanzar DomainException cuando el usuario es menor de edad")
    void saveUser_shouldThrowDomainException_whenUserIsUnderage() {
        validUser.setFechaNacimiento(LocalDate.now().minusYears(17));

        DomainException exception = assertThrows(DomainException.class,
                () -> userUseCase.saveUser(validUser));

        assertEquals(ExceptionConstants.UNDERAGE_USER_MESSAGE, exception.getMessage());
        verify(userPersistencePort, never()).saveUser(any());
        verify(passwordEncoderPort, never()).encode(anyString());
    }

    @Test
    @DisplayName("Debe lanzar DomainException cuando el usuario tiene 17 años y 364 días")
    void saveUser_shouldThrowDomainException_whenUserIsOneYearShort() {
        validUser.setFechaNacimiento(LocalDate.now().minusYears(18).plusDays(1));

        assertThrows(DomainException.class, () -> userUseCase.saveUser(validUser));
        verify(userPersistencePort, never()).saveUser(any());
    }

    @Test
    @DisplayName("Debe aceptar usuario con exactamente 18 años (caso límite)")
    void saveUser_shouldNotThrow_whenUserIsExactly18() {
        validUser.setFechaNacimiento(LocalDate.now().minusYears(18));
        when(passwordEncoderPort.encode(anyString())).thenReturn("encoded");

        assertDoesNotThrow(() -> userUseCase.saveUser(validUser));
        verify(userPersistencePort, times(1)).saveUser(validUser);
    }

    @Test
    @DisplayName("No debe llamar al encoderPort cuando el usuario es menor de edad")
    void saveUser_shouldNotCallEncoder_whenUserIsUnderage() {
        validUser.setFechaNacimiento(LocalDate.now().minusYears(16));

        assertThrows(DomainException.class, () -> userUseCase.saveUser(validUser));

        verifyNoInteractions(passwordEncoderPort);
        verifyNoInteractions(userPersistencePort);
    }
}
