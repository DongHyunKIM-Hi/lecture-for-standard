package org.example.lectureforstandard.global.exception;

// 실습 3 — 어떤 커스텀 예외가 발생하더라도 항상 동일한 형태로 응답하기 위한 구조
public record ErrorResponse(int status, String message) {
}
