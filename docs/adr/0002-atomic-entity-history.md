# ADR 0002 — Atomic entity history for MVP
Status: Provisional.

Store a bounded-in-practice sequence of canon revisions embedded in each entity
document. Spring Data's @Version provides optimistic compare-and-swap semantics:
a fact revision and its history entry persist in ONE MongoDB document update.
No distributed transaction or external replica set is required.

Tradeoff: MongoDB documents have a 16 MiB size limit. Before history grows large,
migrate revisions into a dedicated event collection using a transactional outbox
or MongoDB transactions. Until then, do not treat this storage design as a
general-purpose unlimited event store.

API scope is universe-scoped. The service does not authenticate callers yet;
it must not be exposed on a public network without an auth layer.
