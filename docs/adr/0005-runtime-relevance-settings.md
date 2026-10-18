# ADR 0005 — Configurable relevance scoring

Status: Proposed for implementation in the context assembly milestone.

## Decision
Retrieval scoring and selection knobs must be configuration-driven, never
embedded as literal values in ranking code.

Use a version-controlled YAML baseline and optional, validated runtime
overrides persisted in MongoDB. An effective configuration is an immutable
snapshot created at the beginning of every retrieval operation.

## Scope
- Primary and secondary tag weights; at most three secondary query tags.
- Cross-dimension convergence bonus (distinct tag dimensions).
- One-hop and two-hop relationship weights; configurable maximum traversal depth.
- Maximum candidate facts, event count, and output context budget.
- Mandatory continuity constraints and diversity/deduplication policy.

## Support API (future)
- GET /api/v1/admin/relevance-config: effective configuration, revision, and origin.
- PUT /api/v1/admin/relevance-config: replace validated overrides using
  expected revision; reject stale writes with HTTP 409.
- DELETE /api/v1/admin/relevance-config: revert to version-controlled baseline.
All admin endpoints require authentication and authorization before exposure.
Until then, the file baseline is the supported control surface.

## Validation and observability
- Validate weights as finite nonnegative numbers, sensible upper limits, and
  depth within 0..2 for the MVP; reject invalid configuration atomically.
- Record the effective config revision, matched dimensions, candidate count,
  and selection reason in diagnostic metadata outside the LLM prompt body.
- Unit tests should compare ranking order with fixed fixtures and verify
  precedence, invalid overrides, and reproducibility across snapshots.
- Token budgeting should use the target model tokenizer where available;
  approximate character-based estimates must not be labeled exact.
