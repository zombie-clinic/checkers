package com.example.checkers.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI config() {
    return new OpenAPI()
        .info(new Info().title("Checkers backend API")
            .version("1"));
  }
}
