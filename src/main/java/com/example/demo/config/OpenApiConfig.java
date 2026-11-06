package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadata general que aparece en Swagger UI (título, descripción, versión) y
 * definición del esquema de seguridad Bearer JWT usado por los endpoints
 * protegidos (listas y favoritos).
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI demoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TP3 · Catálogo, Favoritos, Listas y Seguridad JWT")
                        .description("API REST con catálogo público de productos (/api/productos), autenticación con "
                                + "usuario/contraseña y emisión de JWT (/api/auth), y gestión protegida de listas y "
                                + "favoritos con autorización por propiedad de recursos y rol USER/ADMIN.")
                        .version("v3"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresá el token JWT devuelto por POST /api/auth/login")));
    }
}
