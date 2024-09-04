package com.dialodds.seasonsbot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SeasonsbotApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        
        System.setProperty("DISCORD_BOT_TOKEN", dotenv.get("DISCORD_BOT_TOKEN", ""));
        System.setProperty("API_BASE_URL", dotenv.get("API_BASE_URL", "http://localhost:8080"));
        
        SpringApplication.run(SeasonsbotApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ApiClient apiClient(RestTemplate restTemplate) {
        String baseUrl = System.getProperty("API_BASE_URL");
        return new ApiClient(restTemplate, baseUrl);
    }
}