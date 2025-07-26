package com.music.apiservice.config;

import com.music.apiservice.controller.response.GlobalErrorResponse;
import com.music.apiservice.exception.CustomException;
import com.music.apiservice.exception.error.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public Mono<ResponseEntity<GlobalErrorResponse>> handleCustomException(CustomException ex) {

        ErrorCode errorCode = ex.getCode();

        GlobalErrorResponse error = GlobalErrorResponse.of(errorCode.getCode(), errorCode.getMessage(), LocalDateTime.now());

        return Mono.just(ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(error));
    }
}