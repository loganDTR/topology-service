package it.yourcompany.topologyservice.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Neo4j infrastructure configuration.
 *
 * <p>Spring Boot auto-configures the driver and transaction manager from
 * {@code application.yml} ({@code spring.neo4j.*}).  This class activates
 * repository scanning and declarative transaction management so that any
 * future customisation (e.g. custom converters) has a single, obvious home.
 */
@Configuration
@EnableNeo4jRepositories(basePackages = "it.yourcompany.topologyservice.infrastructure.neo4j.repository")
@EnableTransactionManagement
public class Neo4jConfig {
    // Driver and transaction manager are provided by Spring Boot auto-configuration.
    // Add custom Neo4j type converters or mapping configuration here when needed.
}

