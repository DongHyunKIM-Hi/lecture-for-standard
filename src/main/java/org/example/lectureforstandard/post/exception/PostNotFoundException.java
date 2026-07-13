package org.example.lectureforstandard.post.exception;

// 실습 1 — 게시글을 못 찾은 상황을 의미가 드러나게 표현하는 커스텀 예외
public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Long id) {
        super("게시글을 찾을 수 없습니다. id = " + id);
    }
}
