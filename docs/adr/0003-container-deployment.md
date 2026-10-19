# ADR 0003 — Single-host Compose deployment
Status: Accepted for developer / trusted-host MVP.

Use a multi-stage Docker build (Gradle build image; non-root JRE runtime)
and Docker Compose with an unexposed MongoDB service and persistent volume.
Bind HTTP to host loopback only. Keep server setup independent of writing
machine tooling. Authentication and TLS are prerequisites to Internet access.
Prefer versioned image artifacts and immutable image digests before promotion
to a production release. No automatic database migrations are yet present.
