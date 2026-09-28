# Copilot Instructions for topology-service

## Project purpose

This project implements a Java microservice called `topology-service`.

Its purpose is to provide a clean REST API over a Neo4j graph that represents:

- infrastructure assets
- services and dependencies
- business functions
- applications
- user journeys
- teams and ownership

This service is NOT an MCP server.
A separate microservice will expose MCP tools and will call this service via HTTP/Feign.

This service must therefore focus on:
- querying Neo4j
- traversing topology and dependency graphs
- returning structured, LLM-friendly responses
- hiding Cypher and graph complexity behind domain-oriented REST APIs

## Technology stack

Use exactly this stack unless explicitly changed:

- Java 25
- Spring Boot 4.0.3
- Maven
- Spring Web
- Spring Validation
- Spring Data Neo4j
- Neo4j 2026.02.x

Do NOT add MCP-related dependencies in this project.

## Architecture

Use a layered architecture:

- `api`: REST controllers, request/response DTOs, mappers
- `application`: orchestration services and use cases
- `domain`: domain models and query/result contracts
- `infrastructure.neo4j`: graph entities, repositories, custom query adapters, configuration
- `common`: shared utilities

Rules:
- Controllers must stay thin
- Business/query orchestration must stay in application services
- Neo4j-specific code must stay inside infrastructure
- Do not expose raw graph entities directly from REST APIs

## Domain boundaries

This service is responsible for topology and dependency knowledge only.

It must handle:
- topology traversal
- dependency lookup
- downstream impact lookup
- upstream dependency lookup
- business function mapping
- user journey mapping
- ownership lookup

It must NOT be the primary place where incidents/events/anomalies are inferred or stored.

Those are inferred later by an LLM/orchestrator that consumes this service and other microservices.

## Graph model conventions

Important labels:

- `Asset`
- `AKSCluster`
- `VNet`
- `Subnet`
- `VirtualMachine`
- `Database`
- `APIGateway`
- `LoadBalancer`
- `Service`
- `BusinessFunction`
- `Application`
- `UserJourney`
- `Team`

Important relationship types:

- `CONNECTED_TO`
- `CONTAINS`
- `ATTACHED_TO`
- `HOSTED_ON`
- `ROUTES_TO`
- `FRONTS`
- `DEPLOYED_ON`
- `USES`
- `DEPENDS_ON`
- `IMPLEMENTED_BY`
- `EXPOSES`
- `REQUIRES`
- `MANAGES`

The graph already uses stable `id` fields.
Prefer querying by `id`, not by `name`.

## Query design principles

Do not create generic CRUD-only endpoints as the primary interface.

Prefer business-oriented and reasoning-oriented endpoints such as:

- find impacted services from an asset
- find impacted user journeys from an asset
- find upstream dependencies of a service
- find downstream dependents of a service
- explain which business functions are implemented by a service
- explain which journeys require a given business function

Do NOT expose a generic `runCypher` REST endpoint.

## REST response style

Responses must be structured for downstream AI usage:
- deterministic
- concise
- explicit
- stable
- JSON-first

Do not return raw Neo4j internal objects.

Use dedicated response DTOs with clear field names.
Always include identifiers and names where useful.

Prefer response payloads like:
- `asset`
- `impactedServices`
- `impactedBusinessFunctions`
- `impactedJourneys`
- `dependencies`
- `dependents`
- `managedBy`

## Coding style

- Use Java records for DTOs where appropriate
- Use constructor injection only
- Avoid field injection
- Keep methods small and explicit
- Add validation for path/query parameters where meaningful
- Add meaningful JavaDoc only where it clarifies intent
- Avoid overengineering
- Prefer custom repository/query methods for graph traversals
- Prefer explicit Cypher queries for non-trivial graph traversals

## Neo4j usage

Use Spring Data Neo4j where it helps for simple graph persistence and lookup.
For non-trivial traversals, prefer custom Cypher queries and dedicated query adapters.

Important:
- avoid leaking persistence entities outside infrastructure
- keep graph traversal semantics explicit
- design queries around use cases, not around generic graph dumping

## First implementation priorities

Implement first:

1. basic project structure
2. Neo4j configuration
3. health endpoint
4. endpoint to get impacted journeys from an asset
5. endpoint to get service dependencies
6. endpoint to get service dependents
7. endpoint to explain which business functions are linked to a service

## Suggested initial REST endpoints

- `GET /api/v1/assets/{assetId}/impact-summary`
- `GET /api/v1/services/{serviceId}/dependencies?depth=2`
- `GET /api/v1/services/{serviceId}/dependents?depth=2`
- `GET /api/v1/services/{serviceId}/business-functions`
- `GET /api/v1/journeys/{journeyId}/requirements`
- `GET /api/v1/search/services?name=...`

## Non-goals

Do not implement:
- MCP server
- Feign clients
- event inference engine
- alert correlation engine
- authentication/authorization unless explicitly requested
- UI/frontend

## Expected output when generating code

When generating code, prefer complete compilable files.
If a class depends on another class, generate both.
Keep package names coherent and production-like.
Do not generate placeholder pseudocode unless explicitly asked.