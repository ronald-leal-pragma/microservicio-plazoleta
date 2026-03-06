package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IUserServicePort;
import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.ExceptionConstants;
import com.pragma.plazoleta.domain.model.RolModel;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.domain.spi.IPasswordEncoderPort;
import com.pragma.plazoleta.domain.spi.IUserPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;

public class UserUseCase implements IUserServicePort {

    private static final Logger log = LoggerFactory.getLogger(UserUseCase.class);

    private final IUserPersistencePort userPersistencePort;
    private final IPasswordEncoderPort passwordEncoderPort;

    public UserUseCase(IUserPersistencePort userPersistencePort,
                       IPasswordEncoderPort passwordEncoderPort) {
        this.userPersistencePort = userPersistencePort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public void saveUser(UserModel userModel) {
        log.info("[USE CASE] Validando edad del propietario: fechaNacimiento={}", userModel.getFechaNacimiento());
        validateAge(userModel.getFechaNacimiento());
        log.info("[USE CASE] Validación de edad OK");

        log.debug("[USE CASE] Encriptando contraseña");
        String encodedPassword = passwordEncoderPort.encode(userModel.getClave());
        userModel.setClave(encodedPassword);

        log.info("[USE CASE] Asignando rol: {}", ExceptionConstants.ROL_PROPIETARIO);
        RolModel rolPropietario = new RolModel(
                ExceptionConstants.ROL_PROPIETARIO_ID,
                ExceptionConstants.ROL_PROPIETARIO,
                "Rol de propietario de restaurante"
        );
        userModel.setRol(rolPropietario);

        log.info("[USE CASE] Persistiendo usuario: correo={}, documento={}",
                userModel.getCorreo(), userModel.getDocumentoDeIdentidad());
        userPersistencePort.saveUser(userModel);
    }

    private void validateAge(LocalDate fechaNacimiento) {
        int age = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        log.debug("[USE CASE] Edad calculada: {} años", age);
        if (age < 18) {
            log.warn("[USE CASE] Propietario rechazado por ser menor de edad: {} años", age);
            throw new DomainException(ExceptionConstants.UNDERAGE_USER_MESSAGE);
        }
    }
}
