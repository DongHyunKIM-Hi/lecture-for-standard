package org.example.lectureforstandard.global.filter;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// LoginCheckFilter가 Attribute에 담아둔 값을 Controller에서 꺼내 쓸 수 있는지 확인하는 실습용 API예요.
@RestController
@RequestMapping("/api/filter-practice")
public class FilterPracticeController {

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(HttpServletRequest request) {
        Object userId = request.getAttribute(LoginCheckFilter.USER_ID_ATTRIBUTE);
        return Map.of("userId", String.valueOf(userId));
    }
}
