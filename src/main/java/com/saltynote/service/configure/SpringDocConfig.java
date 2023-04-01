package com.saltynote.service.configure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI saltyNoteOpenAPI() {
        return new OpenAPI().info(new Info().title("SaltyNote API")
            .description("SpringDoc for SaltyNote API")
            .version("v0.3.0")
            .license(new License().name("MIT")
                .url("https://github.com/SaltyNote/saltynote-service/blob/master/LICENSE")));
    }

    @Bean
    public GroupedOpenApi homeApi() {
        return GroupedOpenApi.builder().group("HomeController").pathsToMatch("/").build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
            .group("UserController")
            .pathsToMatch("/password/**", "/password", "/signup", "/login", "/email/verification", "/refresh_token",
                    "/account/**")
            .build();
    }

    @Bean
    public GroupedOpenApi noteApi() {
        return GroupedOpenApi.builder().group("NoteController").pathsToMatch("/note/**", "/notes", "/note").build();
    }

}
