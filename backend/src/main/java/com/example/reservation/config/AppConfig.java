package com.example.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {
    /** 日時はすべて日本時間で扱う（テストで差し替えやすいようBean化） */
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Asia/Tokyo"));
    }
}
