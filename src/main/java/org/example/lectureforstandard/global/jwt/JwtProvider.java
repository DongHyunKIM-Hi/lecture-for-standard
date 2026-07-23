package org.example.lectureforstandard.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// JWT를 "발급"하고 "검증"하는 역할만 담당해요. 필터/컨트롤러는 이 클래스를 불러서 쓰기만 해요.
@Component
public class JwtProvider {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtProvider(
            @Value("${jwt.secret-key}") String secretKeyValue,
            @Value("${jwt.expiration-millis}") long expirationMillis) {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyValue.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    // 로그인 성공 시 회원번호(userId)와 권한(role)을 페이로드(클레임)에 담아 토큰을 만들어요.
    public String issueToken(Long userId, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // 서명이 비밀 키와 맞는지, 만료되지 않았는지 검증하고 페이로드(Claims)를 꺼내줘요.
    // 위조됐거나 만료된 토큰이면 io.jsonwebtoken 쪽에서 JwtException을 던져요. (호출하는 필터에서 처리)
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        return claims.get(CLAIM_USER_ID, Long.class);
    }

    public String getRole(Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }
}
