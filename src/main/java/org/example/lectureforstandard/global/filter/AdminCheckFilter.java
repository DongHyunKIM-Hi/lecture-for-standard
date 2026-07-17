package org.example.lectureforstandard.global.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// 관리자 권한 검사 필터 — 요청 헤더의 userId 값이 7이면 관리자, 그 외에는 일반 유저로 취급해요.
// 관리자 전용 API(/api/filter-practice/admin)만 이 필터의 검사를 받고, 나머지 API는 그냥 통과해요.
@Component
@Order(3)
@Slf4j
public class AdminCheckFilter implements Filter {

    private static final String ADMIN_ONLY_PATH = "/api/filter-practice/admin";
    private static final String USER_ID_HEADER = "userId";
    private static final String ADMIN_USER_ID = "7";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 관리자 권한이 필요 없는 API는 검사 없이 그냥 통과시켜요.
        if (!request.getRequestURI().equals(ADMIN_ONLY_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader(USER_ID_HEADER);

        if (ADMIN_USER_ID.equals(userId)) {
            // 관리자면 filterChain.doFilter()를 호출해서 다음 단계(Controller)로 넘겨줘요.
            log.info("[AdminCheckFilter] 관리자(userId={}) 통과", userId);
            filterChain.doFilter(request, response);
            return;
        }

        // 관리자가 아니면 filterChain.doFilter()를 아예 호출하지 않아요.
        // 그래서 요청이 여기서 멈추고, Controller까지 도달하지 못한 채 바로 응답이 나가요.
        log.info("[AdminCheckFilter] 권한 없음(userId={}) - 요청 차단", userId);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write("권한이 없습니다.");
    }
}
