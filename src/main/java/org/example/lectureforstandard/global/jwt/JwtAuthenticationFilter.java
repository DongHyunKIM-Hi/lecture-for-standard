package org.example.lectureforstandard.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// JWT 인증 필터 — Authorization 헤더의 "Bearer {토큰}"을 꺼내 서명과 만료를 검증해요.
// 실패하면(토큰 없음/위조/만료) Controller까지 가지 않고 여기서 401로 막아요.
//
// [11회차] 10회차까지는 이 필터가 @Component로 자동 등록되어 "모든 요청"에 독립적으로 동작했지만,
// 이제는 Spring Security의 필터 체인(SecurityFilterChain) 안에 직접 배치할 거예요.
// (등록 방법은 global.security.SecurityConfig 참고) 그래서 @Component/@Order는 제거했어요 —
// 그대로 뒀다면 Security 체인 밖에서 한 번, 체인 안에서 또 한 번 총 두 번 실행됐을 거예요.
//
// 검증 성공 시 담아두는 곳도 바뀌었어요. request Attribute 대신, 인증된 사람의 정보를
// SecurityContext(보관함)에 넣어둬요. 그러면 뒤에 오는 인가 검사(authorizeHttpRequests)나
// Controller/Service 어디서든 request를 다시 까볼 필요 없이 "지금 요청한 사람"을 꺼내 쓸 수 있어요.
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    // 이 실습(11회차)의 주인공인 jwt-practice API만 검사해요. 다른 회차 실습 API까지 여기서
    // 막아버리면 SecurityConfig의 anyRequest().permitAll()이 무색해지니, 그쪽은 그냥 통과시켜요.
    private static final String JWT_PRACTICE_PREFIX = "/api/jwt-practice";
    // 로그인(토큰 발급) API는 아직 토큰이 없는 게 당연하니 검사 대상에서 빼요.
    private static final String LOGIN_PATH = "/api/jwt-practice/login";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        if (!requestUri.startsWith(JWT_PRACTICE_PREFIX) || LOGIN_PATH.equals(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (token == null) {
            log.info("[JwtAuthenticationFilter] 토큰 없음 - 요청 차단");
            sendUnauthorized(response, "토큰이 없습니다.");
            return;
        }

        try {
            Claims claims = jwtProvider.parseClaims(token);
            Long userId = jwtProvider.getUserId(claims);
            String role = jwtProvider.getRole(claims);

            // 여기가 이번 실습의 핵심이에요 - 검증에 성공한 인증 정보를 SecurityContext에 채워 넣어요.
            // principal 자리에 userId를, authorities 자리에 "ROLE_" + role을 담아둬요.
            // (Spring Security의 hasRole("ADMIN")은 내부적으로 "ROLE_ADMIN" 문자열을 찾기 때문에
            //  이렇게 "ROLE_" 접두사를 직접 붙여줘야 해요.)
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("[JwtAuthenticationFilter] 토큰 검증 통과 - userId={}, role={}", userId, role);
        } catch (JwtException e) {
            // 서명이 안 맞거나(위조), 만료됐거나 - 이유는 다양하지만 결론은 "믿을 수 없는 토큰"이라는 거예요.
            log.info("[JwtAuthenticationFilter] 토큰 검증 실패 - {}", e.getMessage());
            sendUnauthorized(response, "유효하지 않은 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // "Bearer {토큰}" 형태의 헤더 값에서 순수 토큰 문자열만 잘라내요.
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write(message);
    }
}
