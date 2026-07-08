package org.example.lectureforstandard.comment.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.lectureforstandard.post.model.entity.Post;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    // fetch = EAGER → comment를 조회하는 즉시 post도 함께(또는 이어서) 로딩됨
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")  // DB에서 사용할 FK 컬럼 이름
    private Post post;             // Long 대신 Post 객체로!

    public Comment(String content) {
        this.content = content;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
