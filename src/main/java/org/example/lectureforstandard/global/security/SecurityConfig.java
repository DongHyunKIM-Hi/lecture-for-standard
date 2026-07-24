package org.example.lectureforstandard.global.security;

import org.example.lectureforstandard.global.jwt.JwtAuthenticationFilter;
import org.example.lectureforstandard.global.jwt.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// [11회차] 실습 — 우리가 만든 JwtAuthenticationFilter를, Spring Security가 미리 세워둔
// "필터들의 군대(Security Filter Chain)"의 적당한 자리에 끼워 넣는 설정이에요.
@Configuration
public class SecurityConfig {

    private static final String LOGIN_PATH = "/api/jwt-practice/login";
    private static final String ADMIN_PATH = "/api/jwt-practice/admin";
    private static final String JWT_PRACTICE_PATH = "/api/jwt-practice/**";

    // JwtAuthenticationFilter는 더 이상 @Component가 아니에요 (JwtAuthenticationFilter 참고).
    // 그래서 이 설정 클래스가 직접 빈으로 만들어서 아래 필터 체인에 등록해줘요.
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtProvider jwtProvider) {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                // JWT는 서버가 세션을 들고 있지 않는 무상태(Stateless) 방식이에요.
                // 그래서 세션 기반 로그인 화면(formLogin), 세션을 전제로 한 CSRF 방어, 세션 생성이
                // 전부 필요 없어서 꺼줘요.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 인증(Authentication) 이전 - 로그인(토큰 발급)은 토큰이 없는 게 당연하니
                        // 누구나 호출할 수 있어야 해요.
                        .requestMatchers(LOGIN_PATH).permitAll()
                        // 인가(Authorization) 실습 - 인증(로그인)뿐 아니라 ADMIN 권한까지 있어야 들어올 수 있어요.
                        .requestMatchers(ADMIN_PATH).hasRole("ADMIN")
                        // 인증(Authentication) 실습 - 로그인만 했으면(=토큰만 유효하면) 누구나 들어올 수 있어요.
                        .requestMatchers(JWT_PRACTICE_PATH).authenticated()
                        // 이번 실습과 무관한 다른 회차의 API들은 그대로 열어둬요.
                        .anyRequest().permitAll()
                )
                // 우리가 만든 JWT 필터를, Security가 로그인 폼 처리에 쓰는 필터 바로 앞에 세워요.
                // 그래야 뒤쪽(체인 끝부분)의 authorizeHttpRequests 인가 검사가 실행되기 전에,
                // JwtAuthenticationFilter가 먼저 토큰을 검증해서 SecurityContext를 채워둘 수 있어요.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
