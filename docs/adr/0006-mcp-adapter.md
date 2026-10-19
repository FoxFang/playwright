# ADR 0006 — MCP as a transport adapter
Status: Accepted as an architectural boundary; implementation staged.

## Decision
Playwright's domain services own entity resolution, ranked scene context,
event querying, validation and canon commits. REST controllers and MCP tools
are thin transport adapters over the same services. Never call REST from an
in-process MCP tool or duplicate retrieval logic in transport classes.

## MCP tool plan
Read-only (first):
- `get_scene_context`: scoped universe, primary tag, up to three secondary
  tags, explicit context limits; returns narrator-facing versioned JSON.
- `search_events`: universe, tag/time filters, pagination and compact summaries.
- `lookup_entity`: universe, identifier/name/alias and ambiguity reporting.

Mutation (after proposal/validation workflow exists):
- `propose_canon_update`: staged proposal with evidence and expected revisions.
- `commit_canon_update`: an approved and validated proposal identifier,
  idempotency token and appropriate authorization.

## Prompt lifecycle
MCP makes tools discoverable; it does not guarantee that a model invokes
`get_scene_context` before every response. For strict preloading, a narrator
orchestration layer MUST explicitly invoke it before assembling the prompt.
A post-narration stage determines whether canon changes are warranted; Java
validates and applies approved proposals. Narrative content is untrusted data,
never instruction text.

## Deployment/security
Plan Spring AI's WebMVC Streamable HTTP transport as an optional MCP adapter.
Verify actual Spring AI/BOM compatibility with our Spring Boot baseline before
adding the starter. MCP endpoints must not be exposed merely by setting an
environment variable; require an authentication gateway, authorization for
write tools, Origin validation, TLS and network controls. Existing Compose
remains localhost-only until security and protocol interoperability tests pass.

## Tests
Contract fixtures for tool schemas/JSON shapes, matching REST vs MCP domain
results, authorization, stale revisions, idempotent writes, malformed inputs,
and resource limits. Include an MCP client interoperability test in CI.
