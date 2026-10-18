# Playwright

Playwright is a model-agnostic persistent canon API for AI-assisted text roleplaying.
It keeps canonical world facts separate from a narrator's generated prose.

## MVP capabilities
- Create independent universes.
- Create universe-scoped character/world entities with named string facts.
- Retrieve individual entities and compact scene context by explicit cast IDs (max 20).
- Revise facts with required source and reason, optimistic revisions, and historical entries.
- Reject stale writes with HTTP 409 and cross-universe lookups with HTTP 404.

## Running
Requires Java 21, Gradle 8.14+ and MongoDB 7 (or compatible).
Set `MONGODB_URI` if not using the default `mongodb://localhost:27017/playwright`.

```sh
gradle bootRun
gradle test
```

Tests use Testcontainers and require a Docker-compatible daemon. There is not yet
a checked-in Gradle wrapper; generate one with `gradle wrapper` using a compatible
Gradle installation.

### Example API
```sh
curl -X POST http://localhost:8080/api/v1/universes \
  -H 'Content-Type: application/json' -d '{"name":"Project Delta"}'
# Save returned universe id as U.
curl -X POST http://localhost:8080/api/v1/universes/$U/entities \
  -H 'Content-Type: application/json' \
  -d '{"kind":"character","name":"Scott Winstead","facts":{"callsign":"Fang"}}'
# Save returned entity id as E.
curl -X POST http://localhost:8080/api/v1/universes/$U/scene-context \
  -H 'Content-Type: application/json' -d '{"entityIds":["'$E'"]}'
curl -X POST http://localhost:8080/api/v1/universes/$U/entities/$E/revisions \
  -H 'Content-Type: application/json' \
  -d '{"expectedRevision":0,"source":"player-confirmed","reason":"New established fact","facts":{"squadron":"Fox"}}'
```

## Architecture and limitations
See [ADRs](docs/adr). The MVP uses an embedded entity change history,
not a separate scalable event stream. The API currently has **no authentication**;
do not expose it to the public internet. No LLM tool integration, semantic conflict
analysis, or automatic canon promotion has been implemented yet.
