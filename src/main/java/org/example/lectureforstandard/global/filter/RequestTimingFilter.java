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

// 요청 처리 시간을 재는 필터 — filterChain.doFilter()를 기준으로 앞/뒤에서 로직이 실행되는
// 필터의 기본 동작을 보여줘요. @Order(1)이라 세 필터 중 가장 먼저 실행돼요.
@Component
@Order(1)
@Slf4j
public class RequestTimingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        long startTime = System.currentTimeMillis();
        log.info("[RequestTimingFilter] 시작");

        filterChain.doFilter(request, response);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("[RequestTimingFilter] 종료 - 처리 시간: {}ms", elapsed);
    }
}
