package org.example.lectureforstandard.comment.model.dto;

import org.example.lectureforstandard.comment.model.entity.Comment;

public record CommentResponse(Long id, String content) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getContent());
    }
}
