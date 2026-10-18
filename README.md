# Playwright

Playwright is a framework for AI-assisted, persistent text roleplaying. It supplies structured canonical world and character data, event history, and continuity checkpoints so narration can respect established facts and meaningful consequences across long-running campaigns.

## Goals
- Keep multiple fictional universes and campaigns isolated.
- Store canonical world, character, relationship, and event records.
- Retrieve compact, contextual JSON for the active scene.
- Propose and review canon changes rather than silently rewriting history.
- Preserve provenance, revisions, and narrative decisions.
- Remain model-agnostic and separate orchestration from storytelling.

## Proposed MVP
A Spring Boot API backed by MongoDB, with versioned schemas and endpoints for universes, entities, events, retrieval, and continuity checkpoints. The detailed API contract and persistence model will be developed on a separate branch.

## Status
Early design and discovery. No production API exists yet.

## Security
No secrets, tokens, private story material, or credentials should be committed. Repository visibility and deployments should be reviewed before adding private campaign content.
