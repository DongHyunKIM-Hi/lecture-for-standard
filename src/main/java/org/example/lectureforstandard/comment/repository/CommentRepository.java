package org.example.lectureforstandard.comment.repository;

import org.example.lectureforstandard.comment.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
