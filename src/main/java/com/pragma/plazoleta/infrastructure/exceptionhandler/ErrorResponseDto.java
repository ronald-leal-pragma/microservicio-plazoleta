package com.pragma.plazoleta.infrastructure.exceptionhandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {
    private int status;
    private String error;
    private String message;
    private String code;
    private String details;
    private String timestamp;
    private String path;
}
