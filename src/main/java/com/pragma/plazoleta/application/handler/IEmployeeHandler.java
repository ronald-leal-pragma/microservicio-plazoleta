package com.pragma.plazoleta.application.handler;

import com.pragma.plazoleta.application.dto.request.EmployeeRequestDto;
import com.pragma.plazoleta.application.dto.response.EmployeeResponseDto;

public interface IEmployeeHandler {
    EmployeeResponseDto createEmployee(EmployeeRequestDto employeeRequestDto);
}