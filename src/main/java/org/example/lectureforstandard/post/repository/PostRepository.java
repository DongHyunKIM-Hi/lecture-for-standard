package org.example.lectureforstandard.post.repository;

import java.util.List;
import org.example.lectureforstandard.post.model.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 해결 방법 1) Fetch Join — JPQL에 JOIN FETCH를 직접 명시
    // post와 comments를 하나의 SQL(JOIN)로 한 번에 가져오기 때문에 N+1이 발생하지 않음
    @Query("select distinct p from Post p join fetch p.comments")
    List<Post> findAllWithCommentsUsingFetchJoin();

    // 해결 방법 2) @EntityGraph — 어노테이션만으로 "이 연관관계도 같이 가져와줘" 지정
    // 내부 동작은 Fetch Join과 동일함
    @EntityGraph(attributePaths = "comments")
    @Query("select p from Post p")
    List<Post> findAllWithCommentsUsingEntityGraph();
}
