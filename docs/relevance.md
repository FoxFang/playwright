# Relevance configuration and first-stage context retrieval

Defaults are read from `src/main/resources/relevance-defaults.yml`. A single
MongoDB document in `relevance_overrides` with id `global` can override any
subset of fields. Absent override fields inherit YAML defaults; numeric zero is
an explicit override. Invalid merged values are rejected.

To enable the admin API, set `PLAYWRIGHT_ADMIN_TOKEN` to a high-entropy secret in
the server environment and restrict access to the host (Compose binds localhost).
Without this variable, all admin API calls return 503. Send the token in the
`X-Playwright-Admin-Token` header over a protected connection. Never commit
that token to this repository. Rotating/restricting credentials and stronger
authentication are required before deploying this beyond a trusted host.

`GET /api/v1/admin/relevance-config` returns the effective snapshot.
`PUT` accepts `{"expectedRevision":0,"overrides":{"primaryTagWeight":15}}`.
`DELETE` clears overrides and restores defaults. Runtime overrides are
stored in MongoDB and survive container restarts.

`POST /api/v1/universes/{universeId}/events` stores explicitly submitted
tagged events with real-world `recordedAt` and optional in-world `worldTime`.
`POST /api/v1/universes/{universeId}/context/resolve` accepts
`{"primary":"event:first-sortie","secondary":["character:scott","character:winter"]}`.

**Current limitation:** retrieval matches event tags, scores primary/secondary
convergence, and returns a bounded number of summaries. It does not yet
expand graph edges, auto-detect canon-worthy events, produce character records,
or enforce a real tokenizer-based context budget. Events supplied to the write
endpoint are expected to have been confirmed by the narrator/player.
