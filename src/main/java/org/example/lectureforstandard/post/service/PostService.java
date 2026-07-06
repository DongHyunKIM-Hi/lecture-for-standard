package org.example.lectureforstandard.post.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.example.lectureforstandard.comment.model.entity.Comment;
import org.example.lectureforstandard.comment.repository.CommentRepository;
import org.example.lectureforstandard.post.model.entity.Post;
import org.example.lectureforstandard.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public Post createPost(String title) {
        return postRepository.save(new Post(title));
    }

    @Transactional(readOnly = true)
    public Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("post not found: " + postId));
    }

    // 실습 1 — Cascade: addComment로 묶어두면 post만 저장해도 댓글까지 함께 INSERT 돼요.
    @Transactional
    public Post addComment(Long postId, String content) {
        Post post = getPost(postId);
        post.addComment(new Comment(content));
        return post;
    }

    // 실습 2 — orphanRemoval: 컬렉션에서만 제거해도 트랜잭션 커밋 시 DELETE가 자동 실행돼요.
    @Transactional
    public Post removeComment(Long postId, Long commentId) {
        Post post = getPost(postId);
        Comment target = post.getComments().stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("comment not found: " + commentId));
        post.removeComment(target);
        return post;
    }

    // 실습 3 — 연관관계 주인이 아닌 쪽(post.getComments())만 건드리면 FK(post_id)가 저장되지 않아요.
    @Transactional
    public Comment addCommentWithoutOwner(Long postId, String content) {
        Post post = getPost(postId);
        Comment comment = new Comment(content);
        post.getComments().add(comment);
        return commentRepository.save(comment);
    }
}
