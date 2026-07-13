package org.example.lectureforstandard.comment.model.dto;

import org.example.lectureforstandard.comment.exception.InvalidCommentContentException;

public record CreateCommentRequest(String content) {

    public void checkValid() {
        if (content == null || content.isBlank()) {
            throw new InvalidCommentContentException();
        }
    }
}
