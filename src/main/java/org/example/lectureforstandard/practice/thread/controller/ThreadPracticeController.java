package org.example.lectureforstandard.practice.thread.controller;

import java.util.Map;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/practice/threads")
@RequiredArgsConstructor
public class ThreadPracticeController {

    private final @Qualifier("viva1ThreadPool") ThreadPoolTaskExecutor viva1ThreadPool;
    private final @Qualifier("viva2ThreadPool") ThreadPoolTaskExecutor viva2ThreadPool;

    // 실습 2: 아무 지연 없이, 요청마다 어떤 쓰레드가 붙는지 확인
    @GetMapping("/warmup")
    public Map<String, String> warmup() {
        String threadName = Thread.currentThread().getName();
        log.info("[웜업] 요청을 처리한 쓰레드: {}", threadName);
        return Map.of("thread", threadName);
    }

    // 실습 1: 일부러 3초 지연을 주고, 쓰레드 풀이 꽉 찼을 때 요청이 대기하는 것을 체감
    @GetMapping("/slow")
    public Map<String, String> slow() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[SLOW] {} 쓰레드가 요청 처리를 시작합니다.", threadName);
        Thread.sleep(3000);
        log.info("[SLOW] {} 쓰레드가 요청 처리를 완료했습니다.", threadName);
        return Map.of("thread", threadName);
    }

    // 실습 3: 서로 다른 커스텀 쓰레드 풀(viva-1-, viva-2-)에 작업을 맡겨서 쓰레드 이름 접두사 비교
    @GetMapping("/custom-pool/{poolNumber}")
    public Map<String, String> runOnCustomPool(@PathVariable int poolNumber) throws Exception {
        ThreadPoolTaskExecutor executor = switch (poolNumber) {
            case 1 -> viva1ThreadPool;
            case 2 -> viva2ThreadPool;
            default -> throw new IllegalArgumentException("poolNumber는 1 또는 2만 가능합니다.");
        };

        String requestThreadName = Thread.currentThread().getName();
        log.info("[커스텀 풀 {}] 요청을 받은 쓰레드: {}", poolNumber, requestThreadName);

        Future<String> future = executor.submit(() -> {
            String workerThreadName = Thread.currentThread().getName();
            log.info("[커스텀 풀 {}] 작업을 처리한 쓰레드: {}", poolNumber, workerThreadName);
            return workerThreadName;
        });
        String workerThreadName = future.get();

        return Map.of(
                "requestThread", requestThreadName,
                "workerThread", workerThreadName
        );
    }
}
