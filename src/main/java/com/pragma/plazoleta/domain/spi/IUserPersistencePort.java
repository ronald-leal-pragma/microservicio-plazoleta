package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.model.UserModel;

import java.util.Optional;

public interface IUserPersistencePort {
    Optional<UserModel> findUserById(Long id);
    Optional<UserModel> findUserByEmail(String email);
    UserModel saveUser(UserModel userModel);
}
