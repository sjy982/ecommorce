package com.ecommerce.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class DispatcherConfig {
    @Bean(name = "paymentExecutor")
    public ThreadPoolTaskExecutor paymentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50); //기본 유지되는 스레드 수
        executor.setMaxPoolSize(50); //최대 스레드 수
        executor.setQueueCapacity(50); //대기 큐 크기
        return executor;
    }

    @Bean(name = "paymentDispatcherExecutor")
    public ThreadPoolTaskExecutor paymentDispatcherExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        return executor;
    }
}
