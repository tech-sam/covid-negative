package com.covid19negative.common.exception.handler;

import com.covid19negative.common.exception.ApiErrorResopnse;
import com.covid19negative.common.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    public ApiErrorResopnse handleCustomError(CustomException error) {
        Class<? extends CustomException> errorClass = error.getClass();
        if (errorClass.isAnnotationPresent(ResponseStatus.class)) {
            ResponseStatus responseStatus = errorClass.getAnnotation(ResponseStatus.class);
            return new ApiErrorResopnse(responseStatus.code().value(), error.getMessage());
        }
        return new ApiErrorResopnse(HttpStatus.INTERNAL_SERVER_ERROR.value(), error.getMessage());
    }
}
