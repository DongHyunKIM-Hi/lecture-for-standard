package org.example.lectureforstandard.post;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.example.lectureforstandard.comment.model.entity.Comment;
import org.example.lectureforstandard.comment.repository.CommentRepository;
import org.example.lectureforstandard.post.model.entity.Post;
import org.example.lectureforstandard.post.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class PostCommentAssociationTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    // 실습 1 — Cascade 확인 (postRepository.save() 한 번으로 댓글까지)
    @Test
    @Transactional
    void testCascade() {
        Post post = new Post("JPA 공부 정리");
        post.addComment(new Comment("1차 캐시 이해함"));
        post.addComment(new Comment("변경 감지 이해함"));

        Post saved = postRepository.save(post); // 한 번만 저장
        // 콘솔: INSERT 게시글 1번, INSERT 댓글 2번 자동으로 나와요

        assertEquals(2, saved.getComments().size());
    }

    // 실습 2 — orphanRemoval 확인 (관계 끊으면 자동 DELETE)
    @Test
    @Transactional
    void testOrphanRemoval() {
        Post post = new Post("고아 객체 테스트");
        post.addComment(new Comment("삭제될 댓글"));
        post.addComment(new Comment("남을 댓글"));
        postRepository.saveAndFlush(post);

        System.out.println("댓글 수: " + post.getComments().size());
        post.getComments().remove(0); // 첫 번째 댓글과 관계 끊기
        postRepository.flush();
        // flush 시점 → DELETE 자동 실행

        assertEquals(1, post.getComments().size());
    }

    // 실습 3 — 연관관계 주인 확인 (주인 아닌 쪽으로 설정하면?)
    @Test
    @Transactional
    void testOwnerSide() {
        Post post = new Post("연관관계 주인 테스트");
        postRepository.saveAndFlush(post);

        Comment comment = new Comment("이 댓글은 어디로?");

        // 주인 아닌 쪽에만 추가
        post.getComments().add(comment); // DB에 반영 안 됨

        // vs 주인 쪽에 설정
        // comment.setPost(post);  // 이렇게 해야 FK 저장됨
        Comment saved = commentRepository.saveAndFlush(comment);

        assertNull(saved.getPost()); // post_id가 null인지 확인해요
    }
}
