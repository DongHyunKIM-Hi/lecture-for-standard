# 8회차 실습: 쓰레드 풀 체감하기

이번 실습은 서블릿 컨테이너와 쓰레드 풀 내용을 코드로 직접 체감해보는 실습입니다.
아래 순서(0 → 1 → 2 → 3 → 4)대로 진행하세요.

추가된 코드:
- [ThreadPracticeController.java](src/main/java/org/example/lectureforstandard/practice/thread/controller/ThreadPracticeController.java)
- [CustomThreadPoolConfig.java](src/main/java/org/example/lectureforstandard/practice/thread/config/CustomThreadPoolConfig.java)
- [application.yml](src/main/resources/application.yml) — `server.tomcat.threads.max: 2` 로 일부러 작게 설정해둠

---

## 0. 몸풀기 — 동시 요청에 서로 다른(그러나 재사용되는) 쓰레드가 붙는 것 확인하기

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

## 1. 메인 실습 — 쓰레드 풀 한계 체감하기

`application.yml`에 이미 아래처럼 설정되어 있습니다.

```yaml
server:
  tomcat:
    threads:
      min-spare: 1
      max: 2   # 일부러 2로 작게 설정
```

지연이 있는 API로 실험합니다.

```
GET http://localhost:8080/api/practice/threads/slow
```

1. 서버를 재시작합니다 (`server.tomcat.threads.max` 는 재시작해야 반영됩니다).
2. Postman Collection Runner(또는 여러 탭)로 `/api/practice/threads/slow` 를 **동시에 4~5번** 요청합니다.
3. 콘솔 로그를 확인하세요.

```
[SLOW] http-nio-8080-exec-1 쓰레드가 요청 처리를 시작합니다.
[SLOW] http-nio-8080-exec-2 쓰레드가 요청 처리를 시작합니다.
(... 3초 후 ...)
[SLOW] http-nio-8080-exec-1 쓰레드가 요청 처리를 완료했습니다.
[SLOW] http-nio-8080-exec-1 쓰레드가 요청 처리를 시작합니다.   ← 대기하던 3번째 요청
```

→ `exec-1`, `exec-2` 딱 2개의 쓰레드 이름만 번갈아 찍히고, 나머지 요청은 응답이 늦게 오는 것(=작업 큐에서 대기)을 체감하세요.

**비교 실험**: `max: 2` 를 `max: 10` 이나 `max: 20` 으로 바꾸고 서버를 재시작한 뒤, 같은 방식으로 4~5개 동시 요청을 다시 보내서 응답 속도 차이를 비교해보세요.

---

## 2. 커스텀 ThreadPoolTaskExecutor 비교하기

`CustomThreadPoolConfig` 에 이름이 다른 쓰레드 풀 2개가 정의되어 있습니다.

| 빈 이름 | corePoolSize | maxPoolSize | 쓰레드 이름 접두사 |
|---|---|---|---|
| `viva1ThreadPool` | 5 | 10 | `viva-1-` |
| `viva2ThreadPool` | 10 | 20 | `viva-2-` |

각 풀에 작업을 맡겨보는 API입니다.

```
GET http://localhost:8080/api/practice/threads/custom-pool/1
GET http://localhost:8080/api/practice/threads/custom-pool/2
```

호출 후 콘솔 로그와 응답을 확인하세요.

```
[커스텀 풀 1] 요청을 받은 쓰레드: http-nio-8080-exec-1
[커스텀 풀 1] 작업을 처리한 쓰레드: viva-1-1
```

→ 요청을 받은 쓰레드(Tomcat 쓰레드)와 실제 작업을 처리한 쓰레드(커스텀 풀 쓰레드)가 다르다는 것, 그리고 풀마다 이름 접두사(`viva-1-` / `viva-2-`)가 다르게 찍히는 것을 확인하세요.

---

## 3. (선택) Tomcat 대신 다른 서블릿 컨테이너로 바꿔보기

> ⚠️ **Undertow 관련 안내**: 원래 계획은 Tomcat → Undertow 교체였는데, 확인해보니 이 프로젝트가 쓰는 **Spring Boot 4.1.0부터는 `spring-boot-starter-undertow` 스타터가 더 이상 제공되지 않습니다** (Maven Central 기준 Undertow 스타터는 3.5.x 대까지만 존재). 그래서 이 실습은 **Jetty**로 대체합니다. "Tomcat이 아닌 다른 서블릿 컨테이너로 바꿔도 애플리케이션 코드는 그대로 동작한다"는 핵심 포인트는 Jetty로도 동일하게 확인할 수 있습니다.
>
> 이 실습을 하면 `server.tomcat.threads.*` 설정은 더 이상 의미가 없습니다. 위 1~2번 실습을 먼저 끝낸 뒤에 진행하세요.

### 3-1. build.gradle 수정

[build.gradle](build.gradle) 에 아래처럼 주석으로 준비해두었습니다. 주석을 풀어서 적용하세요.

```groovy
implementation('org.springframework.boot:spring-boot-starter-webmvc') {
    exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
}
implementation 'org.springframework.boot:spring-boot-starter-jetty'
```

(기존의 `implementation 'org.springframework.boot:spring-boot-starter-webmvc'` 한 줄은 지우거나 주석 처리)

### 3-2. application.yml 수정

[application.yml](src/main/resources/application.yml) 의 `server.tomcat` 블록을 주석 처리하고, 아래 `server.jetty` 블록의 주석을 해제하세요.

```yaml
server:
  jetty:
    threads:
      min: 4   # Jetty는 acceptor/selector용 쓰레드도 별도로 필요해서 1~2처럼 극단적으로 작게 주면 서버가 아예 안 뜹니다
      max: 8   # 실습용으로 작게 설정
```

### 3-3. 재시작 후 로그 확인

```
./gradlew bootRun
```

시작 로그에 `Tomcat` 대신 아래처럼 찍히는지 확인하세요.

```
Jetty started on port 8080 (http/1.1) with context path '/'
```

그 다음 1번 실습(`/api/practice/threads/slow`)을 **10개 동시 요청**으로 다시 돌려보세요 (`max: 8` 이므로 4~5개로는 큐잉이 잘 안 보입니다). Tomcat 때와 마찬가지로 쓰레드가 다 차면 나머지 요청이 대기하는지 비교해보세요. (Jetty의 워커 쓰레드 이름은 `qtp<해시>-<번호>` 형태로 찍힙니다. 예: `qtp2041036732-20`)

이 프로젝트로 직접 검증해본 결과, `max: 8` 설정에서 `/api/practice/threads/slow` 를 10번 동시 요청하면 총 약 6초가 걸리고(8개는 즉시 처리, 나머지 2개는 대기 후 처리), 콘솔에는 `qtp...` 형태의 쓰레드 이름 6~8개가 반복적으로 찍힙니다.
