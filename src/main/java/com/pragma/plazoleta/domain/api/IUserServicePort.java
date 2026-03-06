package com.pragma.plazoleta.domain.api;

import com.pragma.plazoleta.domain.model.UserModel;

public interface IUserServicePort {
    void saveUser(UserModel userModel);
}
