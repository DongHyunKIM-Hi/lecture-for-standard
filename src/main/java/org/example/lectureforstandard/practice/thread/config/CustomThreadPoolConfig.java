package org.example.lectureforstandard.practice.thread.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class CustomThreadPoolConfig {

    @Bean(name = "viva1ThreadPool")
    public ThreadPoolTaskExecutor viva1ThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("viva-1-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "viva2ThreadPool")
    public ThreadPoolTaskExecutor viva2ThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("viva-2-");
        executor.initialize();
        return executor;
    }
}
