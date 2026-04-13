# topology-service architecture notes

## Intent

This service is a topology query service over Neo4j.

It is meant to be consumed by another microservice that exposes MCP tools to an LLM.

The LLM should never talk directly to Neo4j.
The MCP-facing service should never contain graph traversal logic that belongs here.

## Responsibility split

### topology-service
- query graph
- traverse dependencies
- compute structured impact summaries
- expose REST

### mcp-gateway-service
- expose MCP tools
- call topology-service via Feign/HTTP
- orchestrate responses for LLM usage

## Design principle

Topology is persistent knowledge.
Events are inferred outside this service by combining topology responses with signals from other microservices.