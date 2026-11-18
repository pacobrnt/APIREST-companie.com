package fr.saintmichel.sirene;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Ajoute le bouton "Authorize" dans Swagger UI : on y colle le jeton obtenu auprès de Keycloak. */
@Configuration
@OpenAPIDefinition(info = @Info(title = "API Sirene"), security = @SecurityRequirement(name = "keycloak"))
@SecurityScheme(name = "keycloak", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
