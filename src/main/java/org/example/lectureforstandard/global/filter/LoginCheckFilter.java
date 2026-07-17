package org.example.lectureforstandard.global.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// 🛠️ 실습 — 로그인 검사 필터 직접 만들어보기
@Component
@Order(2)
@Slf4j
public class LoginCheckFilter implements Filter {

    public static final String USER_ID_ATTRIBUTE = "userId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        // TODO(수강생 실습 1, 2): Controller 진입 전/후로 로그를 각각 남겨서
        //  filterChain.doFilter()를 기준으로 순서가 어떻게 나뉘는지 확인해보세요.
        //  (예: log.info("[LoginCheckFilter] 시작"), log.info("[LoginCheckFilter] 종료"))
        //  RequestTimingFilter와 함께 실행해서 로그가 양파 껍질처럼 겹쳐 찍히는지도 확인해보세요.

        // TODO(수강생 실습 4): 실제로는 토큰을 검증해서 사용자 id를 알아내야 하지만,
        //  지금은 임의의 값을 request.setAttribute(USER_ID_ATTRIBUTE, ...)로 담아보세요.

        filterChain.doFilter(request, response);
    }
}
