package com.saltynote.service.swagger;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConf {

  @Bean
  public UiConfiguration tryItOutConfig() {
    return UiConfigurationBuilder.builder().supportedSubmitMethods(new String[0]).build();
  }

  @Bean
  public Docket api() {

    ApiInfo apiInfo =
        new ApiInfo(
            "SaltyNote Service API",
            "A quick swagger doc for all endpoints",
            "0.0.1",
            "https://www.saltynote.com/terms",
            new Contact("SaltyNote", "https://github.com/SaltyNote", "contact@saltynote.com"),
            "MIT",
            "https://github.com/SaltyNote/saltynote-service/blob/master/LICENSE",
            Collections.emptyList());
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(apiInfo)
        .select()
        .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
        .paths(PathSelectors.any())
        .build();
  }
}
