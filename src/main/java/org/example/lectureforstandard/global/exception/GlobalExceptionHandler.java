package org.example.lectureforstandard.global.exception;

import org.example.lectureforstandard.comment.exception.CommentNotFoundException;
import org.example.lectureforstandard.comment.exception.InvalidCommentContentException;
import org.example.lectureforstandard.post.exception.PostNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePostNotFound(PostNotFoundException e) {
        ErrorResponse response = new ErrorResponse(404, e.getMessage());
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFound(CommentNotFoundException e) {
        ErrorResponse response = new ErrorResponse(404, e.getMessage());
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(InvalidCommentContentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCommentContent(InvalidCommentContentException e) {
        ErrorResponse response = new ErrorResponse(400, e.getMessage());
        return ResponseEntity.status(400).body(response);
    }
}
