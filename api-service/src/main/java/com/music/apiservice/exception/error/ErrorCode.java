package com.music.apiservice.exception.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String getCode();
    String getMessage();
    HttpStatus getHttpStatus();
}