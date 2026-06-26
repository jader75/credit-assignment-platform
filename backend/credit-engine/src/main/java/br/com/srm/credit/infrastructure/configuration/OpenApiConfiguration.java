package br.com.srm.credit.infrastructure.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info =
                @Info(
                        title = "SRM Credit Engine",
                        version = "0.1.0",
                        description = "API de simulacao e operacao da plataforma de cessao de credito.",
                        contact = @Contact(name = "SRM Asset")))
public class OpenApiConfiguration {}
