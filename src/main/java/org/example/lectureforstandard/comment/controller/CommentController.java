package org.example.lectureforstandard.comment.controller;

import lombok.RequiredArgsConstructor;
import org.example.lectureforstandard.comment.model.dto.CommentDetailResponse;
import org.example.lectureforstandard.comment.model.dto.CreateCommentRequest;
import org.example.lectureforstandard.post.model.dto.PostResponse;
import org.example.lectureforstandard.post.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final PostService postService;

    // 실습 1 — addComment()로 추가 후 post만 저장해도 cascade로 댓글까지 INSERT 돼요.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createComment(@PathVariable Long postId, @RequestBody CreateCommentRequest request) {
        request.checkValid();
        return PostResponse.from(postService.addComment(postId, request.content()));
    }

    // 실습 2 — 컬렉션에서 제거하면 orphanRemoval로 DELETE가 자동 실행돼요.
    @DeleteMapping("/{commentId}")
    public PostResponse deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        return PostResponse.from(postService.removeComment(postId, commentId));
    }

    // 실습 3 — 주인이 아닌 쪽만 변경하면 post_id가 null로 저장되는 것을 확인해요.
    @PostMapping("/without-owner")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDetailResponse createCommentWithoutOwner(@PathVariable Long postId,
            @RequestBody CreateCommentRequest request) {
        return CommentDetailResponse.from(postService.addCommentWithoutOwner(postId, request.content()));
    }
}
