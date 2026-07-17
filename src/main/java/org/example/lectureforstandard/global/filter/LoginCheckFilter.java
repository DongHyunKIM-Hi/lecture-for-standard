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



        filterChain.doFilter(request, response);
    }
}
