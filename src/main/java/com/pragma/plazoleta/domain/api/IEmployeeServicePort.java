package com.pragma.plazoleta.domain.api;

import com.pragma.plazoleta.domain.model.UserModel;

public interface IEmployeeServicePort {
    UserModel createEmployee(UserModel employeeModel, Long ownerId);
}