package com.music.apiservice.exception.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LikeError implements ErrorCode {

    ALREADY_LIKE_ERROR("LIK_001", "이미 좋아요를 누르셨습니다")
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    LikeError(String code,
              String message) {

        this.code = code;
        this.message = message;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
