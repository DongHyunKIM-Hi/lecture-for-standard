# 8회차 실습: 쓰레드 풀 체감하기

이번 실습은 서블릿 컨테이너와 쓰레드 풀 내용을 코드로 직접 체감해보는 실습입니다.
아래 진행 순서(**실습 2 → 실습 1 → 실습 3 → 실습 4 → 실습 5**)대로 진행하세요.
(번호는 [ThreadPracticeController.java](src/main/java/org/example/lectureforstandard/practice/thread/controller/ThreadPracticeController.java)의 코드 주석/로그에 찍히는 번호와 동일합니다. "실습 2(몸풀기)"를 "실습 1(메인)"보다 먼저 하는 이유는 몸풀기이기 때문입니다.)

추가된 코드:
- [ThreadPracticeController.java](src/main/java/org/example/lectureforstandard/practice/thread/controller/ThreadPracticeController.java)
- [CustomThreadPoolConfig.java](src/main/java/org/example/lectureforstandard/practice/thread/config/CustomThreadPoolConfig.java)
- [application.yml](src/main/resources/application.yml) — `server.tomcat.threads.max: 2` 로 일부러 작게 설정해둠

---

## 실습 2. 몸풀기 — 동시 요청에 서로 다른(그러나 재사용되는) 쓰레드가 붙는 것 확인하기

지연 없이 쓰레드 이름만 로그로 찍는 API입니다.

```
GET http://localhost:8080/api/practice/threads/warmup
```

이 API를 5~6번 연속으로 호출하면서 콘솔 로그를 보세요.

```
[웜업] 요청을 처리한 쓰레드: http-nio-8080-exec-1
[웜업] 요청을 처리한 쓰레드: http-nio-8080-exec-2
[웜업] 요청을 처리한 쓰레드: http-nio-8080-exec-1
```

→ 요청마다 쓰레드 이름이 몇 가지로 정해져 있고, 계속 재사용되는 걸 확인하세요. (쓰레드 풀 재사용 개념)

---

## 실습 1. 메인 실습 — 쓰레드 풀 한계 체감하기

`application.yml`에 이미 아래처럼 설정되어 있습니다.

```yaml
server:
  tomcat:
    threads:
      min-spare: 1
      max: 2   # 일부러 2로 작게 설정
```

지연이 있는 API로 실험합니다. (`Thread.sleep(30000)` — 라이브 강의에서 화면을 보여주며 설명할 시간을 벌기 위해 30초로 설정되어 있습니다. 빠르게 확인하고 싶다면 코드에서 지연 시간을 줄여도 됩니다.)

```
GET http://localhost:8080/api/practice/threads/slow
```

1. 서버를 재시작합니다 (`server.tomcat.threads.max` 는 재시작해야 반영됩니다).
2. Postman Collection Runner(또는 여러 탭)로 `/api/practice/threads/slow` 를 **동시에 3~4번** 요청합니다.
3. 콘솔 로그를 확인하세요.

```
[SLOW] http-nio-8080-exec-1 쓰레드가 요청 처리를 시작합니다.
[SLOW] http-nio-8080-exec-2 쓰레드가 요청 처리를 시작합니다.
(... 30초 후 ...)
[SLOW] http-nio-8080-exec-1 쓰레드가 요청 처리를 완료했습니다.
[SLOW] http-nio-8080-exec-1 쓰레드가 요청 처리를 시작합니다.   ← 대기하던 3번째 요청
```

→ `exec-1`, `exec-2` 딱 2개의 쓰레드 이름만 번갈아 찍히고, 나머지 요청은 응답이 늦게 오는 것(=작업 큐에서 대기)을 체감하세요. 3번째 요청은 앞의 두 요청 중 하나가 끝나야(=최대 30초 후) 시작됩니다.

**비교 실험**: `max: 2` 를 `max: 10` 이나 `max: 20` 으로 바꾸고 서버를 재시작한 뒤, 같은 방식으로 동시 요청을 다시 보내서 응답 속도 차이를 비교해보세요.

---

## 실습 3. 커스텀 ThreadPoolTaskExecutor 비교하기 (블로킹 버전)

`CustomThreadPoolConfig` 에 이름이 다른 쓰레드 풀 2개가 정의되어 있습니다.

| 빈 이름 | corePoolSize | maxPoolSize | 쓰레드 이름 접두사 |
|---|---|---|---|
| `viva1ThreadPool` | 5 | 10 | `viva-1-` |
| `viva2ThreadPool` | 10 | 20 | `viva-2-` |

각 풀에 "3초 걸리는 무거운 작업"을 맡겨보는 API입니다.

```
GET http://localhost:8080/api/practice/threads/custom-pool/1
GET http://localhost:8080/api/practice/threads/custom-pool/2
```

호출 후 콘솔 로그와 응답을 확인하세요.

```
[커스텀 풀 1 - 블로킹] 요청을 받은 쓰레드: http-nio-8080-exec-1 (작업이 끝날 때까지 반납되지 않음)
[커스텀 풀 1 - 블로킹] 작업을 처리한 쓰레드: viva-1-1
(... 3초 후 ...)
[커스텀 풀 1 - 블로킹] 요청 쓰레드(http-nio-8080-exec-1)가 이제서야 반납됩니다.
```

→ 요청을 받은 쓰레드(Tomcat 쓰레드)와 실제 작업을 처리한 쓰레드(커스텀 풀 쓰레드)가 다르다는 것, 그리고 풀마다 이름 접두사(`viva-1-` / `viva-2-`)가 다르게 찍히는 것을 확인하세요.

여기까지만 보면 "커스텀 쓰레드 풀로 무거운 작업을 분리했다"고 착각하기 쉽습니다. **하지만 로그를 자세히 보면, 요청을 받은 Tomcat 쓰레드가 작업이 끝날 때까지(3초 내내) 반납되지 않고 있습니다.** 이 부분은 실습 5에서 다시 짚어봅니다.

---

## 실습 4. (선택) Tomcat 대신 다른 서블릿 컨테이너로 바꿔보기

> ⚠️ **Undertow 관련 안내**: 원래 계획은 Tomcat → Undertow 교체였는데, 확인해보니 이 프로젝트가 쓰는 **Spring Boot 4.1.0부터는 `spring-boot-starter-undertow` 스타터가 더 이상 제공되지 않습니다** (Maven Central 기준 Undertow 스타터는 3.5.x 대까지만 존재). 그래서 이 실습은 **Jetty**로 대체합니다. "Tomcat이 아닌 다른 서블릿 컨테이너로 바꿔도 애플리케이션 코드는 그대로 동작한다"는 핵심 포인트는 Jetty로도 동일하게 확인할 수 있습니다.
>
> 이 실습을 하면 `server.tomcat.threads.*` 설정은 더 이상 의미가 없습니다. 위 실습 2·1을 먼저 끝낸 뒤에 진행하세요.

### 4-1. build.gradle 수정

[build.gradle](build.gradle) 에 아래처럼 주석으로 준비해두었습니다. 주석을 풀어서 적용하세요.

```groovy
implementation('org.springframework.boot:spring-boot-starter-webmvc') {
    exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
}
implementation 'org.springframework.boot:spring-boot-starter-jetty'
```

(기존의 `implementation 'org.springframework.boot:spring-boot-starter-webmvc'` 한 줄은 지우거나 주석 처리)

### 4-2. application.yml 수정

[application.yml](src/main/resources/application.yml) 의 `server.tomcat` 블록을 주석 처리하고, 아래 `server.jetty` 블록의 주석을 해제하세요.

```yaml
server:
  jetty:
    threads:
      min: 4   # Jetty는 acceptor/selector용 쓰레드도 별도로 필요해서 1~2처럼 극단적으로 작게 주면 서버가 아예 안 뜹니다
      max: 8   # 실습용으로 작게 설정
```

### 4-3. 재시작 후 로그 확인

```
./gradlew bootRun
```

시작 로그에 `Tomcat` 대신 아래처럼 찍히는지 확인하세요.

```
Jetty started on port 8080 (http/1.1) with context path '/'
```

그 다음 실습 1(`/api/practice/threads/slow`)을 **10개 동시 요청**으로 다시 돌려보세요 (`max: 8` 이므로 3~4개로는 큐잉이 잘 안 보입니다). Tomcat 때와 마찬가지로 쓰레드가 다 차면 나머지 요청이 대기하는지 비교해보세요. (Jetty의 워커 쓰레드 이름은 `qtp<해시>-<번호>` 형태로 찍힙니다. 예: `qtp2041036732-20`)

---

## 실습 5. 실습 3 다시 보기 — "효율적으로 쓰겠다"더니 왜 두 풀을 다 잡고 있지?

커스텀 쓰레드 풀을 만드는 이유는 **오래 걸리는 작업을 Tomcat 풀과 분리된 별도 공간에서 처리**하기 위함입니다. 그런데 실습 3의 `/custom-pool/{n}` 코드를 다시 보면:

```java
Future<String> future = executor.submit(() -> { ... });
String workerThreadName = future.get();   // ← 여기서 블로킹!
```

`future.get()`은 **블로킹 호출**이라서, 커스텀 풀 쓰레드가 3초짜리 작업을 하는 동안 **Tomcat 요청 쓰레드도 같이 3초를 그 자리에서 대기**합니다. 즉 작업 하나를 처리하는 데 Tomcat 쓰레드 1개 + 커스텀 풀 쓰레드 1개, **총 2개의 쓰레드를 동시에 점유**합니다. 무거운 작업을 별도 공간으로 분리해서 Tomcat 쓰레드를 아끼려던 원래 목적과 반대로, 오히려 자원을 더 쓰고 있는 셈입니다.

### 5-1. 직접 눈으로 비효율 확인하기

`tomcat.threads.max: 2`인 상태에서 블로킹 버전(`/custom-pool/1`)에 **4개 동시 요청**을 보내보세요.

```
GET http://localhost:8080/api/practice/threads/custom-pool/1   (x4 동시)
```

→ 실제로 돌려보면 Tomcat 쓰레드가 2개뿐이라 **2라운드로 나뉘어 처리**되고, 총 소요 시간이 **약 6초**가 걸립니다(3초짜리 작업이 2번 순차적으로 대기). `/api/practice/threads/slow`(실습 1)와 똑같은 큐잉 패턴이 그대로 나타나는 겁니다 — 커스텀 풀을 쓰든 안 쓰든 Tomcat 쓰레드 관점에서는 차이가 없다는 뜻입니다.

### 5-2. DeferredResult로 개선하기 — `/custom-pool-async/{poolNumber}`

같은 3초짜리 작업을 커스텀 풀에 맡기되, `future.get()`으로 기다리는 대신 `DeferredResult`를 리턴해서 Tomcat 쓰레드를 **즉시 반납**하는 버전입니다.

```java
@GetMapping("/custom-pool-async/{poolNumber}")
public DeferredResult<Map<String, String>> runOnCustomPoolAsync(@PathVariable int poolNumber) {
    ...
    DeferredResult<Map<String, String>> deferredResult = new DeferredResult<>();
    executor.execute(() -> {
        Thread.sleep(HEAVY_JOB_MILLIS);
        deferredResult.setResult(...);   // 작업이 끝나면 그때 결과를 채워넣는다
    });
    return deferredResult;   // Tomcat 쓰레드는 여기서 바로 반납됨
}
```

같은 방식으로 4개 동시 요청을 보내보세요.

```
GET http://localhost:8080/api/practice/threads/custom-pool-async/1   (x4 동시)
```

→ 실제로 돌려보면 Tomcat 쓰레드가 여전히 2개뿐인데도 **4개 요청이 전부 약 3초 만에 동시에 끝납니다.** Tomcat 쓰레드가 작업을 커스텀 풀에 넘기자마자 즉시 풀로 돌아가서 다음 요청을 받기 때문에, 이론상 Tomcat 풀 크기와 무관하게(커스텀 풀 용량이 허락하는 한) 무거운 작업을 동시에 처리할 수 있습니다.

콘솔 로그를 보면 차이가 더 명확합니다.

```
[커스텀 풀 1 - 비동기] 요청을 받은 쓰레드: http-nio-8080-exec-1 (곧바로 반납됩니다)
[커스텀 풀 1 - 비동기] 작업을 처리한 쓰레드: viva-1-1
[커스텀 풀 1 - 비동기] 요청을 받은 쓰레드: http-nio-8080-exec-1 (곧바로 반납됩니다)   ← 몇 ms 만에 같은 Tomcat 쓰레드가 다음 요청을 또 받음
```

블로킹 버전은 같은 Tomcat 쓰레드가 다음 요청을 받기까지 **3초**가 걸리지만, 비동기 버전은 **수 ms**만에 다음 요청을 받습니다. (이 프로젝트로 직접 검증: 블로킹 4개 동시 요청 = 약 6.2초 / 2라운드, 비동기 4개 동시 요청 = 약 3.0초 / 전부 동시 처리.)

### 정리

| | 블로킹 (`/custom-pool/{n}`, 실습 3) | 비동기 (`/custom-pool-async/{n}`, 실습 5) |
|---|---|---|
| Tomcat 쓰레드 | 작업이 끝날 때까지 점유 | 작업을 맡기자마자 즉시 반납 |
| 동시 점유 쓰레드 수 | Tomcat 1개 + 커스텀 풀 1개 (총 2개) | 커스텀 풀 1개만 |
| Tomcat 쓰레드 2개로 4개 동시 요청 처리 시간 | 약 6초 (2라운드) | 약 3초 (전부 동시) |

→ 커스텀 쓰레드 풀을 "제대로" 활용하려면, 무거운 작업을 맡긴 뒤 결과를 `future.get()`으로 기다리지 말고 `DeferredResult`/`CompletableFuture`/`Callable` 같은 **비동기 처리 방식**으로 Tomcat 쓰레드를 즉시 돌려줘야 합니다.
