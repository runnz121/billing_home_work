package com.music.apiservice.exception;

import com.music.apiservice.exception.error.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode code;

    public CustomException(ErrorCode code) {
        this.code = code;
    }
}
