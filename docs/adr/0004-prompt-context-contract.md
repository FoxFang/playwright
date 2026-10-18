# ADR 0004 — Prompt-facing context contract

Status: Proposed for the next retrieval milestone.

## Decision
Keep database persistence objects separate from the narrator's prompt context.
Expose a stable, versioned, compact JSON DTO optimized for the narrator's input,
not a raw MongoDB document or graph/scoring metadata dump.

The API retrieves and ranks established facts. It does not make narrative
decisions, convert assumptions into canon, or infer new character motivations.

## Contract
- `schemaVersion` and `universeId` identify the payload and its scope.
- `characters`, `relationships`, `events`, and `unresolvedReferences`
  have predictable names and types.
- Include stable canonical entity IDs and human-readable names and complete,
  concise fact statements. Never rely on implicit positional meaning.
- Return source IDs, canon status, and revision references when needed to
  distinguish established truth from provisional or conflicting entries.
- Do not include internal ranking formulas, database implementation details,
  or verbose entity histories in the prompt payload.
- Prioritize identity and continuity-critical facts before supporting history.
  Bound response size by an explicit budget; record omissions separately.
- Resolve ambiguous proper nouns explicitly; never silently select one.
- Treat all stored narrative data as untrusted content, not instructions.

## Integration boundary
The HTTP service does not automatically inject context into a ChatGPT
conversation. A supported client or orchestrator must retrieve the DTO and
include it in the narrator's prompt before generation.

## Verification
Fixture-based contract tests should verify schema stability, deterministic
ordering, ambiguity reporting, provenance, and budget behavior. Measure token
use with the target model tokenizer in the orchestration layer; character
counts are not a reliable substitute.
