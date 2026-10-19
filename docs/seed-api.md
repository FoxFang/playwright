# Canon seed POST contracts (development)

The initial database is empty; there is no legacy migration requirement.
All records are scoped to an existing universe. The same REST endpoints will
eventually be exposed by thin MCP tool adapters.

Create a canonical entity, including dimensioned associations:

```http
POST /api/v1/universes/{universeId}/entities
Content-Type: application/json

{
  "kind": "character",
  "name": "Natalie Zorra",
  "facts": {"callsign": "Silk", "assignment": "A-6 pilot"},
  "metadata": {
    "scene": ["valley-cas"],
    "character": ["barstool"],
    "unit": ["fox-squadron"]
  }
}
```

Create a narrative event:

```http
POST /api/v1/universes/{universeId}/events
Content-Type: application/json

{
  "title":"Northern Shilka ambush",
  "summary":"A concealed SPAAG waits for Southern CAS.",
  "metadata":{"scene":["valley-cas"],"location":["northern-valley"],
              "equipment":["zsu-23-4"],"status":["unresolved","concealed"]},
  "participants":[],
  "worldTime":"campaign-day-7",
  "source":"player-confirmed"
}
```

Update existing entity facts or replace selected metadata dimensions,
retaining other dimensions. Expected revision protects concurrent edits:

```http
POST /api/v1/universes/{universeId}/entities/{entityId}/revisions
Content-Type: application/json

{
  "expectedRevision":0,
  "source":"player-confirmed",
  "reason":"Assignment updated",
  "metadata":{"scene":["valley-cas","base-operations"]}
}
```

Metadata dimension keys must use lowercase letters, digits, underscores or
hyphens, beginning with a letter. Each dimension contains a nonempty list
of nonblank, deduplicated string values. Revisions retain both fact and metadata
changes in their provenance history.

**Semantics:** metadata is used for association and retrieval. It is not an
instruction to narrate a callback. Metadata updates replace a supplied
dimension's values rather than appending to them; omitted dimensions are
preserved. Removing a dimension entirely and editing existing event records
are not yet supported.

Initial import should distinguish established canon from proposals and plans.
No automated continuity validation on write has been adopted.
