package com.mlops.pacientes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pacientesOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Predicción de Pacientes")
                        .description("Servicio para consultar pacientes, hábitos y niveles estimados de enfermedad.")
                        .version("1.0.0"));
    }
}
