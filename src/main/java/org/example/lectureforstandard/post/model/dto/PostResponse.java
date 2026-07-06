package org.example.lectureforstandard.post.model.dto;

import java.util.List;
import org.example.lectureforstandard.comment.model.dto.CommentResponse;
import org.example.lectureforstandard.post.model.entity.Post;

public record PostResponse(Long id, String title, List<CommentResponse> comments) {

    public static PostResponse from(Post post) {
        List<CommentResponse> comments = post.getComments().stream()
                .map(CommentResponse::from)
                .toList();
        return new PostResponse(post.getId(), post.getTitle(), comments);
    }
}
