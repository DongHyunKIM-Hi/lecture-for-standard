package org.example.lectureforstandard.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// JWT 인증 필터 — Authorization 헤더의 "Bearer {토큰}"을 꺼내 서명과 만료를 검증해요.
// 검증에 성공하면 Controller가 쓸 수 있도록 userId/role을 Attribute에 담아 통과시키고,
// 실패하면(토큰 없음/위조/만료) Controller까지 가지 않고 여기서 401로 막아요.
// LoginCheckFilter, AdminCheckFilter와 마찬가지로 OncePerRequestFilter를 상속해서 만들어요.
@Component
@Order(5)
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_ID_ATTRIBUTE = "jwtUserId";
    public static final String ROLE_ATTRIBUTE = "jwtRole";

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    // 로그인(토큰 발급) API는 아직 토큰이 없는 게 당연하니 검사 대상에서 빼요.
    private static final String LOGIN_PATH = "/api/jwt-practice/login";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (LOGIN_PATH.equals(request.getRequestURI())) {
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
            request.setAttribute(USER_ID_ATTRIBUTE, jwtProvider.getUserId(claims));
            request.setAttribute(ROLE_ATTRIBUTE, jwtProvider.getRole(claims));
            log.info("[JwtAuthenticationFilter] 토큰 검증 통과 - userId={}", jwtProvider.getUserId(claims));
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
