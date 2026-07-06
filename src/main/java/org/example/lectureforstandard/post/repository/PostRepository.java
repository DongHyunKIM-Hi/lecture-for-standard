package org.example.lectureforstandard.post.repository;

import org.example.lectureforstandard.post.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

}
