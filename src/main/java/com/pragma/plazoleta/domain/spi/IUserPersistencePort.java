package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.model.UserModel;

import java.util.Optional;

public interface IUserPersistencePort {
    void saveUser(UserModel userModel);
    Optional<UserModel> findUserById(Long id);
}
