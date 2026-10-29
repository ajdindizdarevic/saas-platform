package org.userservice.config;

import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration placeholder.
 *
 * <p>CORS is handled exclusively by the API Gateway ({@code CorsConfig}).
 * Individual microservices must NOT add their own CORS headers; doing so
 * causes "Access-Control-Allow-Origin contains multiple values" browser errors
 * because both the gateway and the service would append the header.
 */
@Configuration
public class OpenApiConfig {
    // CORS filter removed — handled by API Gateway only.
}
