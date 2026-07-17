package org.example.lectureforstandard.global.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// 로그인 검사 필터 — 실제 로그인 검증 대신, 클라이언트가 헤더로 보낸 userId를
// Attribute에 담아 Controller까지 전달하는 흐름을 보여줘요.
// @Order(1)인 RequestTimingFilter 다음, @Order(3)인 AdminCheckFilter보다 먼저 실행돼요.
@Component
@Order(2)
@Slf4j
public class LoginCheckFilter implements Filter {

    public static final String USER_ID_ATTRIBUTE = "userId";
    private static final String USER_ID_HEADER = "userId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        // filterChain.doFilter() 위쪽 = Controller에 들어가기 전에 실행되는 로직
        log.info("[LoginCheckFilter] 시작 - Controller 진입 전");

        // 실제로는 토큰을 검증해서 사용자 id를 알아내지만, 여기서는 클라이언트가 보낸
        // userId 헤더 값을 그대로 믿고 Attribute에 담아둬요.
        // 같은 요청을 처리하는 하나의 쓰레드가 Filter부터 Controller까지 쭉 이어서 담당하기 때문에,
        // 여기서 담아둔 값을 Controller에서 request.getAttribute()로 그대로 꺼내 쓸 수 있어요.
        String userId = ((HttpServletRequest) request).getHeader(USER_ID_HEADER);
        request.setAttribute(USER_ID_ATTRIBUTE, userId);

        filterChain.doFilter(request, response);

        // filterChain.doFilter() 아래쪽 = Controller를 다녀온 뒤에 실행되는 로직
        log.info("[LoginCheckFilter] 종료 - Controller 다녀온 후");
    }
}
