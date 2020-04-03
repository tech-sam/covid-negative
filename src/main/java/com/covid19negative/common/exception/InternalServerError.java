package com.covid19negative.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerError extends CustomException {

    public InternalServerError() {
        super();
    }

    public InternalServerError(String errorMsg) {
        super(errorMsg);
    }

    public InternalServerError(String errorMsg, Throwable error) {
        super(errorMsg, error);
    }

}
