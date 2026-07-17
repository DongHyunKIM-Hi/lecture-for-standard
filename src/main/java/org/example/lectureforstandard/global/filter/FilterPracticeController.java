package org.example.lectureforstandard.global.filter;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 필터가 하는 일을 직접 호출해보면서 눈으로 확인할 수 있는 API예요.
@RestController
@RequestMapping("/api/filter-practice")
public class FilterPracticeController {

    // LoginCheckFilter가 Attribute에 담아둔 userId를 Controller에서 그대로 꺼내 써요.
    // userId 헤더 없이 요청하면 값이 null로 나오는 것도 함께 확인해보세요.
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(HttpServletRequest request) {
        Object userId = request.getAttribute(LoginCheckFilter.USER_ID_ATTRIBUTE);
        return Map.of("userId", String.valueOf(userId));
    }

    // AdminCheckFilter를 통과한 요청(userId 헤더 값이 7)만 이 메서드까지 들어올 수 있어요.
    // 그 외의 요청은 필터 단계에서 이미 403으로 막혀서 여기까지 오지 않아요.
    @GetMapping("/admin")
    public Map<String, Object> getAdminOnlyInfo() {
        return Map.of("message", "관리자만 볼 수 있는 정보예요.");
    }
}
