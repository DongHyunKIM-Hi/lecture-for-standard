package org.example.lectureforstandard.comment.model.dto;

import org.example.lectureforstandard.comment.model.entity.Comment;

public record CommentDetailResponse(Long id, String content, Long postId) {

    public static CommentDetailResponse from(Comment comment) {
        Long postId = comment.getPost() == null ? null : comment.getPost().getId();
        return new CommentDetailResponse(comment.getId(), comment.getContent(), postId);
    }
}
