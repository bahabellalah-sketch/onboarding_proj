package com.onboarding.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get the absolute path to the uploads directory
        String uploadDir = System.getProperty("user.dir") + "/uploads";
        Path uploadPath = Paths.get(uploadDir);

        // Register the resource handler
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath.toAbsolutePath() + "/");
    }
}
