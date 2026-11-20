package com.chubb.claims.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI chubbClaimsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chubb APAC Claims Processing API")
                        .description("REST API for Multi-Market Insurance Claims Processing across MY, SG, TH, HK, AU, and JP. " +
                                "Supports Claimant self-service FNOL submissions and Claims Officer assessments with strict state machine validation.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Chubb APAC Engineering")
                                .url("https://www.chubb.com")));
    }

    @Bean
    public OperationCustomizer addAuthHeaderParameters() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new Parameter()
                    .name("X-User-Id")
                    .in("header")
                    .required(false)
                    .description("Actor identifier (e.g. claimant-101, officer-202)")
                    .example("officer-001"));

            operation.addParametersItem(new Parameter()
                    .name("X-User-Role")
                    .in("header")
                    .required(false)
                    .description("Actor role: CLAIMANT | OFFICER | SYSTEM")
                    .example("OFFICER"));

            return operation;
        };
    }
}
