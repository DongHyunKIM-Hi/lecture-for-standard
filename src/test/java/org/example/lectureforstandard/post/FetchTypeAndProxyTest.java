package org.example.lectureforstandard.post;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.example.lectureforstandard.comment.model.entity.Comment;
import org.example.lectureforstandard.comment.repository.CommentRepository;
import org.example.lectureforstandard.post.model.entity.Post;
import org.example.lectureforstandard.post.repository.PostRepository;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 6회차 실습 — Eager vs Lazy 로딩 시점 비교 / Proxy 확인 / N+1 문제 재현과 해결
 *
 * 모든 테스트는 @Transactional 로 감싸여 있어 DB에는 흔적이 남지 않아요(끝나면 롤백).
 * em.flush() + em.clear() 로 1차 캐시를 비워서, "진짜로 DB까지 다녀오는지"를 매번 확인합니다.
 */
@SpringBootTest
class FetchTypeAndProxyTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
    }

    // ─────────────────────────────────────────────
    // 1. Eager vs Lazy — "언제" 값을 가져오는지 비교
    // ─────────────────────────────────────────────

    @Test
    @Transactional
    void EAGER로_매핑된_Comment_post는_Comment를_조회하는_즉시_함께_로딩된다() {
        Post post = new Post("EAGER 실습용 게시글");
        Comment comment = new Comment("EAGER 실습용 댓글");
        post.addComment(comment);
        postRepository.saveAndFlush(post);
        em.clear(); // 1차 캐시 비우기 → 진짜 DB 조회를 강제

        System.out.println("========== Comment 조회 시작 (post는 EAGER) ==========");
        statistics.clear();

        Comment found = commentRepository.findById(comment.getId()).orElseThrow();

        // findById가 끝난 시점에 이미 post까지 로딩이 끝나 있어야 함(EAGER)
        long queryCountAfterFind = statistics.getPrepareStatementCount();
        System.out.println("findById 직후 쿼리 실행 횟수: " + queryCountAfterFind);
        System.out.println("post 필드가 이미 초기화되어 있는가? " + Hibernate.isInitialized(found.getPost()));

        assertThat(Hibernate.isInitialized(found.getPost())).isTrue();
        // 이미 로딩이 끝났으므로, 여기서 post.getTitle()을 호출해도 추가 쿼리가 나가지 않음
        String title = found.getPost().getTitle();
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(queryCountAfterFind);
        System.out.println("post.getTitle() 호출 후에도 쿼리 횟수 그대로: " + statistics.getPrepareStatementCount());
    }

    @Test
    @Transactional
    void LAZY로_매핑된_Post_comments는_Post를_조회한_시점에는_로딩되지_않는다() {
        Post post = new Post("LAZY 실습용 게시글");
        post.addComment(new Comment("LAZY 실습용 댓글"));
        postRepository.saveAndFlush(post);
        em.clear();

        System.out.println("========== Post 조회 시작 (comments는 LAZY) ==========");
        statistics.clear();

        Post found = postRepository.findById(post.getId()).orElseThrow();

        long queryCountAfterFind = statistics.getPrepareStatementCount();
        System.out.println("findById 직후 쿼리 실행 횟수: " + queryCountAfterFind);
        System.out.println("comments 필드가 이미 초기화되어 있는가? " + Hibernate.isInitialized(found.getComments()));

        // Post만 SELECT 했을 뿐, comments는 아직 Proxy(비어있는 상태) → 초기화 안 됨
        assertThat(Hibernate.isInitialized(found.getComments())).isFalse();

        // 실제로 "사용"하는 순간 SELECT ... WHERE post_id = ? 쿼리가 추가로 나감
        int commentCount = found.getComments().size();

        System.out.println("getComments() 호출 후 쿼리 실행 횟수: " + statistics.getPrepareStatementCount());
        assertThat(Hibernate.isInitialized(found.getComments())).isTrue();
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(queryCountAfterFind + 1);
        assertThat(commentCount).isEqualTo(1);
    }

    // ─────────────────────────────────────────────
    // 2. Proxy 디버깅 실습
    //
    //    아래 테스트에서 "// ▶ 여기에 브레이크포인트" 표시된 두 줄에
    //    각각 브레이크포인트를 걸고 디버그 모드로 실행해보세요.
    //
    //    ⚠️ 주의: found.comments(또는 found.getComments())를 Variables 창에서
    //    "펼치거나(expand)" Evaluate 하지 마세요! IntelliJ는 컬렉션을 화면에
    //    보여주려고 내부적으로 size()/iterator()를 자동 호출하는데, 이게 바로
    //    Proxy를 깨우는 신호라서 여러분이 코드로 접근하기도 전에 디버거가
    //    먼저 초기화를 시켜버려요(심하면 LazyInitializationException도 남).
    //
    //    대신 아래 로컬 변수만 확인하세요 — 부작용이 없어요.
    //    1) 첫 번째 브레이크포인트에서 beforeAccess 변수 값을 확인 → false
    //       (Hibernate.isInitialized()는 내부 플래그만 읽을 뿐 size()/iterator()를
    //        호출하지 않기 때문에 안전해요)
    //    2) getComments().size() 를 Step Over 로 실행한 뒤
    //       두 번째 브레이크포인트에서 afterAccess 변수 값을 확인 → true
    // ─────────────────────────────────────────────

    @Test
    @Transactional
    void Proxy가_실제_사용_시점에_초기화되는_과정을_디버깅으로_확인한다() {
        Post post = new Post("Proxy 디버깅용 게시글");
        post.addComment(new Comment("Proxy 디버깅용 댓글1"));
        post.addComment(new Comment("Proxy 디버깅용 댓글2"));
        postRepository.saveAndFlush(post);
        em.clear();

        Post found = postRepository.findById(post.getId()).orElseThrow();

        boolean beforeAccess = Hibernate.isInitialized(found.getComments()); // ▶ 여기에 브레이크포인트
        System.out.println("접근 전 initialized = " + beforeAccess);
        assertThat(beforeAccess).isFalse();

        int size = found.getComments().size(); // ← 이 줄에서 실제 SELECT 쿼리가 나감

        boolean afterAccess = Hibernate.isInitialized(found.getComments()); // ▶ 여기에 브레이크포인트
        System.out.println("접근 후 initialized = " + afterAccess + ", size = " + size);
        assertThat(afterAccess).isTrue();
    }

    // ─────────────────────────────────────────────
    // 3. N+1 문제 재현
    // ─────────────────────────────────────────────

    @Test
    @Transactional
    void N_PLUS_1_문제가_발생하는_것을_확인한다() {
        int postCount = 5;
        for (int i = 1; i <= postCount; i++) {
            Post post = new Post("게시글 " + i);
            post.addComment(new Comment(i + "번 게시글의 댓글"));
            postRepository.save(post);
        }
        postRepository.flush();
        em.clear();

        System.out.println("========== N+1 재현 시작 ==========");
        statistics.clear();

        var posts = postRepository.findAll();
        for (Post post : posts) {
            // getComments() 를 호출할 때마다 Proxy가 깨어나면서 SELECT가 1번씩 추가로 나감
            System.out.println(post.getTitle() + " - 댓글 " + post.getComments().size() + "개");
        }

        long totalQueryCount = statistics.getPrepareStatementCount();
        System.out.println("총 쿼리 실행 횟수: " + totalQueryCount + " (기대값: findAll 1번 + 게시글 수만큼 N번 = " + (postCount + 1) + "번)");

        // 목록 조회 1번 + 게시글 개수(N)만큼의 추가 조회 = N+1
        assertThat(totalQueryCount).isEqualTo(postCount + 1);
    }

    // ─────────────────────────────────────────────
    // 4. N+1 해결 — Fetch Join / @EntityGraph
    // ─────────────────────────────────────────────

    @Test
    @Transactional
    void FetchJoin으로_N_PLUS_1_문제를_1번의_쿼리로_해결한다() {
        int postCount = 5;
        for (int i = 1; i <= postCount; i++) {
            Post post = new Post("FetchJoin 게시글 " + i);
            post.addComment(new Comment(i + "번 게시글의 댓글"));
            postRepository.save(post);
        }
        postRepository.flush();
        em.clear();

        System.out.println("========== Fetch Join 적용 후 ==========");
        statistics.clear();

        var posts = postRepository.findAllWithCommentsUsingFetchJoin();
        for (Post post : posts) {
            // 이미 JOIN FETCH로 comments까지 함께 로딩되어 있어서 추가 SELECT가 나가지 않음
            System.out.println(post.getTitle() + " - 댓글 " + post.getComments().size() + "개");
        }

        long totalQueryCount = statistics.getPrepareStatementCount();
        System.out.println("총 쿼리 실행 횟수: " + totalQueryCount + " (기대값: 1번)");

        assertThat(totalQueryCount).isEqualTo(1);
    }

    @Test
    @Transactional
    void EntityGraph로_N_PLUS_1_문제를_1번의_쿼리로_해결한다() {
        int postCount = 5;
        for (int i = 1; i <= postCount; i++) {
            Post post = new Post("EntityGraph 게시글 " + i);
            post.addComment(new Comment(i + "번 게시글의 댓글"));
            postRepository.save(post);
        }
        postRepository.flush();
        em.clear();

        System.out.println("========== @EntityGraph 적용 후 ==========");
        statistics.clear();

        var posts = postRepository.findAllWithCommentsUsingEntityGraph();
        for (Post post : posts) {
            System.out.println(post.getTitle() + " - 댓글 " + post.getComments().size() + "개");
        }

        long totalQueryCount = statistics.getPrepareStatementCount();
        System.out.println("총 쿼리 실행 횟수: " + totalQueryCount + " (기대값: 1번)");

        assertThat(totalQueryCount).isEqualTo(1);
    }
}
