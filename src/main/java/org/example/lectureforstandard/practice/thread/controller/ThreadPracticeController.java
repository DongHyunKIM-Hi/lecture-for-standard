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
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@RestController
@RequestMapping("/api/practice/threads")
@RequiredArgsConstructor
public class ThreadPracticeController {

    // 커스텀 풀에 맡기는 작업이 "오래 걸리는 무거운 작업"이라고 가정하기 위한 지연 시간
    private static final long HEAVY_JOB_MILLIS = 10000;

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
        ThreadPoolTaskExecutor executor = resolveExecutor(poolNumber);

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

    // 실습 5: 실습 3과 똑같이 "3초 걸리는 무거운 작업"을 커스텀 풀에 맡기지만,
    // future.get()으로 기다리지 않고 DeferredResult를 사용해 Tomcat 쓰레드를 즉시 반납한다.
    // → 커스텀 풀을 만든 원래 목적(오래 걸리는 작업을 별도 공간에서 처리)에 맞는 진짜 비동기 구조
    @GetMapping("/custom-pool-async/{poolNumber}")
    public DeferredResult<Map<String, String>> runOnCustomPoolAsync(@PathVariable int poolNumber) {
        ThreadPoolTaskExecutor executor = resolveExecutor(poolNumber);

        String requestThreadName = Thread.currentThread().getName();
        log.info("[커스텀 풀 {} - 비동기] 요청을 받은 쓰레드: {} (곧바로 반납됩니다)", poolNumber, requestThreadName);

        DeferredResult<Map<String, String>> deferredResult = new DeferredResult<>();

        executor.execute(() -> {
            String workerThreadName = Thread.currentThread().getName();
            log.info("[커스텀 풀 {} - 비동기] 작업을 처리한 쓰레드: {}", poolNumber, workerThreadName);
            try {
                Thread.sleep(HEAVY_JOB_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                deferredResult.setErrorResult(e);
                return;
            }
            deferredResult.setResult(Map.of(
                    "requestThread", requestThreadName,
                    "workerThread", workerThreadName
            ));
        });

        return deferredResult;
    }

    private ThreadPoolTaskExecutor resolveExecutor(int poolNumber) {
        return switch (poolNumber) {
            case 1 -> viva1ThreadPool;
            case 2 -> viva2ThreadPool;
            default -> throw new IllegalArgumentException("poolNumber는 1 또는 2만 가능합니다.");
        };
    }
}
