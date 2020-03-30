package com.covid19negative.common.exception;

public class CustomException extends RuntimeException {

    public CustomException() {
        super();
    }

    public CustomException(String errorMsg) {
        super(errorMsg);
    }

    public CustomException(String errorMsg, Throwable error) {
        super(errorMsg, error);
    }
}
