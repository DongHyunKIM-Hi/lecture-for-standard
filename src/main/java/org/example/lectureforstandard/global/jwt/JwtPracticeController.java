package org.example.lectureforstandard.global.jwt;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// JWT 발급 -> 필터 검증 -> SecurityContext 활용까지 직접 손으로 확인해볼 수 있는 API예요.
// 1) POST /api/jwt-practice/login?userId=7&role=ADMIN 으로 토큰을 발급받고
// 2) GET /api/jwt-practice/me 요청의 Authorization 헤더에 "Bearer {발급받은 토큰}"을 담아 보내보세요.
//    - 토큰 없이, 또는 위조/만료된 토큰으로 호출하면 JwtAuthenticationFilter가 컨트롤러 진입 전에 401로 막아줘요.
// 3) role=ADMIN으로 받은 토큰으로 GET /api/jwt-practice/admin 을 호출해보고,
//    role=USER 토큰으로도 호출해보세요. 인증(로그인)은 됐어도 권한이 없으면 403으로 막혀요. (인가 실습)
@RestController
@RequestMapping("/api/jwt-practice")
@RequiredArgsConstructor
public class JwtPracticeController {

    private final JwtProvider jwtProvider;

    // 실제로는 아이디/비밀번호를 검증하고 DB에서 회원을 조회하지만,
    // 이 예제에서는 그 과정을 생략하고 파라미터로 받은 값을 그대로 페이로드에 담아 토큰만 발급해요.
    @PostMapping("/login")
    public Map<String, String> login(@RequestParam Long userId, @RequestParam(defaultValue = "USER") String role) {
        String token = jwtProvider.issueToken(userId, role);
        return Map.of("token", token);
    }

    // 이 메서드에 도착했다는 것 자체가 JwtAuthenticationFilter의 검증을 이미 통과했다는 뜻이에요.
    // request Attribute를 다시 까지 않고, SecurityContextHolder에서 바로 "지금 요청한 사람"을 꺼내 써요.
    @GetMapping("/me")
    public Map<String, Object> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Map.of(
                "userId", authentication.getPrincipal(),
                "role", authentication.getAuthorities());
    }

    // 인가(Authorization) 실습용 엔드포인트 - SecurityConfig에서 ADMIN 권한만 들어오도록 막아뒀어요.
    // 인증(로그인)까지는 통과했더라도, role=USER 토큰으로 호출하면 여기까지 오지 못하고 403을 받아요.
    @GetMapping("/admin")
    public Map<String, Object> admin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Map.of(
                "message", "관리자 전용 페이지에 오신 걸 환영해요.",
                "userId", authentication.getPrincipal());
    }
}
