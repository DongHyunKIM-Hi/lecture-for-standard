package org.example.lectureforstandard.post.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.example.lectureforstandard.comment.exception.InvalidCommentContentException;
import org.example.lectureforstandard.comment.model.entity.Comment;
import org.example.lectureforstandard.comment.repository.CommentRepository;
import org.example.lectureforstandard.post.exception.PostNotFoundException;
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
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    @Transactional
    public Post addComment(Long postId, String content) {
        Post post = getPost(postId);
        post.addComment(new Comment(content));
        return post;
    }

    // TODO(수강생 실습): 댓글을 못 찾은 경우 CommentNotFoundException을 만들어 던져보세요.
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

    @Transactional
    public Comment addCommentWithoutOwner(Long postId, String content) {
        Post post = getPost(postId);
        Comment comment = new Comment(content);
        post.getComments().add(comment);
        return commentRepository.save(comment);
    }
}
