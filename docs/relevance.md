# Relevance configuration and first-stage context retrieval

Defaults are read from `src/main/resources/relevance-defaults.yml`. MongoDB
`relevance_overrides` can override any subset of fields. Runtime scoring
remains configurable, and the context response is still bounded by maxEvents.

`POST /api/v1/universes/{universeId}/events` now accepts multidimensional
metadata instead of a flat tags array:

```json
{
  "title":"Northern Shilka deployment",
  "summary":"A concealed SPAAG awaits Southern CAS aircraft.",
  "metadata":{
    "scene":["valley-cas"],
    "character":["northern-commander"],
    "location":["northern-valley"],
    "equipment":["zsu-23-4"],
    "status":["unresolved","concealed"]
  },
  "source":"player-confirmed"
}
```

`POST /api/v1/universes/{universeId}/context/resolve` retains
`{"primary":"scene:valley-cas","secondary":["character:silk","character:barstool"]}`.
The selector syntax is `dimension:value`; the LLM or orchestrator selects
relevant dimensions. Values are matched exactly within that metadata dimension.

MongoDB now filters candidate events within the requested universe using an OR of\ncategory-specific metadata matches (primary and secondary). The result pool is\nbounded and ordered by document ID; Java then ranks that pool using the\nconfigured primary/secondary weights
and cross-dimension bonus. `status` may be queried but does not contribute
a new cross-dimension bonus. No semantic analysis or autonomous state updates
are performed.

**Migration note:** Existing `canon_events` documents using `tags` must
be migrated to `metadata` before retrieval. The existing event POST request
shape is a breaking change. No migration is included in this commit.

**Current limitations:** Matching candidates are capped by maxCandidates before scoring,
so relevant records beyond the cap may be omitted. The configured token budget
is not yet enforced by a tokenizer. Graph expansion and full character payloads
are not yet implemented. Continuity validation on write remains undecided.
