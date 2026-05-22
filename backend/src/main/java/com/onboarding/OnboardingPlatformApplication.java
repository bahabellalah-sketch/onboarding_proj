package com.onboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OnboardingPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnboardingPlatformApplication.class, args);
    }
}
