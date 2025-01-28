package com.example.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * Main application class for the Spring Boot backend.
 * Initializes environment variables and starts the application.
 */
@SpringBootApplication
public class BackendApplication {

    /**
     * Loads environment variables from the `.env` file using Dotenv
     * and sets them as system properties for Spring Boot.
     */
	@PostConstruct
	public void loadEnvironmentVariables() {
		Dotenv dotenv = Dotenv.configure()
				.filename(".env")  // Specify the .env file name (default is ".env")
				.ignoreIfMissing() // Avoid errors if .env is not present
				.load();
	
		// Load MySQL environment variables
		System.setProperty("DB_URL", dotenv.get("DB_URL", "jdbc:mysql://localhost:3306/default_db"));
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME", "default_user"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD", "default_password"));
	
		// Load Redis environment variables
		System.setProperty("REDIS_HOST", dotenv.get("REDIS_HOST", "localhost"));
		System.setProperty("REDIS_PORT", dotenv.get("REDIS_PORT", "6379"));
	
		// Load Server Port
		String serverPort = dotenv.get("SERVER_PORT");
		System.setProperty("SERVER_PORT", serverPort);

		// System.out.println("DB_URL: " + System.getProperty("DB_URL"));
		// System.out.println("SERVER_PORT: " + System.getProperty("SERVER_PORT"));
	
		// Validate critical variables
		validateEnvironmentVariables();
	}
	

    /**
     * Validates that required environment variables are set.
     * Throws an exception if any critical variable is missing.
     */
    private void validateEnvironmentVariables() {
        if (System.getProperty("DB_URL") == null || System.getProperty("DB_USERNAME") == null || System.getProperty("DB_PASSWORD") == null) {
            throw new IllegalStateException("Critical environment variables (DB_URL, DB_USERNAME, DB_PASSWORD) are missing.");
        }
    }

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
