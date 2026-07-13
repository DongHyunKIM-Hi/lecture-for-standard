package org.example.lectureforstandard.global.exception;

import org.example.lectureforstandard.post.exception.PostNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 실습 2 — 컨트롤러마다 반복해서 @ExceptionHandler를 붙이는 대신, 전역에서 한 번만 등록
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePostNotFound(PostNotFoundException e) {
        ErrorResponse response = new ErrorResponse(404, e.getMessage());
        return ResponseEntity.status(404).body(response);
    }
}
