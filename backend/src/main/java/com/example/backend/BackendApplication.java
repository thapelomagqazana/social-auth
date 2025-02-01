package com.example.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        // Load .env file
        Dotenv dotenv = Dotenv.configure()
                              .directory("./") // Ensure this points to the root directory containing the .env file
                              .load();

        // Set environment variables for the application
        System.setProperty("BASE_URL", dotenv.get("BASE_URL"));
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("REDIS_HOST", dotenv.get("REDIS_HOST"));
        System.setProperty("REDIS_PORT", dotenv.get("REDIS_PORT"));
        System.setProperty("SERVER_PORT", dotenv.get("SERVER_PORT"));
        System.setProperty("JWT_SECRET_KEY", dotenv.get("JWT_SECRET_KEY"));
        System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));

        // System.out.println("DB_URL from dotenv: " + dotenv.get("DB_URL"));
        // System.out.println("DB_URL from System properties: " + System.getProperty("DB_URL"));


        SpringApplication.run(BackendApplication.class, args);
    }
}
