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

// 🛠️ 실습 — 관리자 권한 검사 필터 직접 만들어보기
// 클라이언트가 요청 헤더에 userId: 7 을 담아 보내면 관리자, 그 외에는 일반 유저로 취급해서
// 관리자 전용 API는 관리자만 통과시키고, 나머지는 막아보는 실습이에요.
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

        // 관리자 권한이 필요 없는 API는 그냥 통과시켜요.
        if (!request.getRequestURI().equals(ADMIN_ONLY_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }

        // TODO(수강생 실습): request.getHeader(USER_ID_HEADER)로 헤더 값을 읽어보세요.
        //  - 값이 ADMIN_USER_ID("7")와 같으면 관리자이므로 filterChain.doFilter(request, response)를 호출해서 통과시키기
        //  - 그 외에는(값이 없는 경우 포함) filterChain.doFilter()를 호출하지 말고
        //    response.setStatus(HttpServletResponse.SC_FORBIDDEN); 과
        //    response.getWriter().write("권한이 없습니다."); 로 여기서 바로 응답을 끝내보세요.
        //    (Controller까지 못 들어가는 걸 확인해보세요)
    }
}
