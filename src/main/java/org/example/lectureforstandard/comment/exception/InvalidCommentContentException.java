package org.example.lectureforstandard.comment.exception;

public class InvalidCommentContentException extends RuntimeException {

    public InvalidCommentContentException() {
        super("댓글 내용은 비어 있을 수 없습니다.");
    }
}
