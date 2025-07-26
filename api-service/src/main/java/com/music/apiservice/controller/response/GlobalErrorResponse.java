package com.music.apiservice.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GlobalErrorResponse {

    private String code;

    private String message;

    private LocalDateTime timestamp;

    public static GlobalErrorResponse of(String code,
                                         String message,
                                         LocalDateTime timestamp) {

        return new GlobalErrorResponse(code, message, timestamp);
    }
}