package org.example.lectureforstandard.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // 이 필터를 스프링이 자동으로 등록해줘요
@Order(4)
public class SimpleLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청이 컨트롤러에 도착하기 "전"에 실행되는 부분
        System.out.println("[필터 시작] 요청 주소: " + request.getRequestURI());

        // 2. 다음 단계로 넘겨주기 (컨트롤러로 요청 전달)
        filterChain.doFilter(request, response);

        // 3. 응답이 나가기 "직전"에 실행되는 부분
        System.out.println("[필터 끝] 응답 완료: " + request.getRequestURI());
    }
}