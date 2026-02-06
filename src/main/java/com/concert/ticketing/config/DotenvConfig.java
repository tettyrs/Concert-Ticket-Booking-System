package com.concert.ticketing.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads environment variables from .env file before Spring Boot starts.
 * This allows using .env file for local development.
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        try {
            // Load .env file from project root
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing() // Don't fail if .env doesn't exist (e.g., in production)
                    .load();

            // Convert dotenv entries to a Map
            Map<String, Object> dotenvMap = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                dotenvMap.put(entry.getKey(), entry.getValue());
            });

            // Add dotenv properties to Spring Environment with high priority
            environment.getPropertySources().addFirst(
                    new MapPropertySource("dotenvProperties", dotenvMap));

            System.out.println("✅ .env file loaded successfully");
        } catch (Exception e) {
            System.out.println("⚠️ .env file not found or could not be loaded: " + e.getMessage());
            System.out.println("   Using default values or system environment variables");
        }
    }
}
