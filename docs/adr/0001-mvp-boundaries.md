# ADR 0001 — MVP boundaries
Status: Accepted for initial implementation; revisit through discovery.

## Decision
Spring Boot 3 with Java 21, MongoDB, and HTTP/JSON. Universes isolate canon.
Canonical entity snapshots and append-only continuity events are separate.
Narration is never implicitly promoted to canon. Changes require explicit
authored events with an expected revision and a recorded source.

## MVP vertical slice
Create a universe; create and retrieve character/world entities; record
revision-checked canon changes with provenance; request scene context for a
cast of entity identifiers. Return only selected records, not a full lore dump.

## Deferred
Vector retrieval, LLM tool-calling, temporal graph queries, RBAC, semantic
contradiction detection, transcript ingestion, UI and automatic canon promotion.

## Safety
Avoid credentials and private world data in source control. Enforce universe
scope on all reads/writes. Treat all external narrative as untrusted inputs.
