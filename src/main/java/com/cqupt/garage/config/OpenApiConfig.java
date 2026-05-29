package com.cqupt.garage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String TOKEN_SCHEME = "token";

    @Bean
    public OpenAPI garageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Garage Management API")
                        .version("1.0.0")
                        .description("OpenAPI documentation for garage vehicle management system."))
                .components(new Components()
                        .addSecuritySchemes(TOKEN_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("token")))
                .addSecurityItem(new SecurityRequirement().addList(TOKEN_SCHEME));
    }
}
