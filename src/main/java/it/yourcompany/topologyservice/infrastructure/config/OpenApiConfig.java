package it.yourcompany.topologyservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Produces the {@link OpenAPI} metadata bean consumed by springdoc.
 *
 * <p>Exposes title, description, version, and contact so that both the
 * Swagger UI and machine-readable {@code /api-docs} endpoint carry useful
 * service-level context for downstream consumers (e.g. the MCP layer).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI topologyServiceOpenAPI(
            @Value("${spring.application.name:topology-service}") String appName,
            @Value("${server.port:8080}") int port) {

        return new OpenAPI()
                .info(new Info()
                        .title("Topology Service API")
                        .description("""
                                REST API over a Neo4j graph representing infrastructure assets, \
                                services, business functions, applications, user journeys, and teams.
                                
                                Provides topology traversal and dependency analysis endpoints \
                                designed for service-to-service and LLM/AI consumption.
                                Cypher and graph internals are fully hidden behind domain-oriented endpoints.
                                """)
                        .version("0.0.1-SNAPSHOT")
                        .contact(new Contact()
                                .name("Platform Engineering")
                                .email("platform@yourcompany.it")))
                .addServersItem(new Server()
                        .url("http://localhost:" + port)
                        .description("Local development"));
    }
}

