# Small-server deployment

## Host prerequisites
Linux host with Docker Engine + Docker Compose plugin, roughly 2 GB RAM or more
and persistent disk storage. Image builds are more demanding than running the
containers; build off-host when RAM is particularly constrained.

## Start
Clone the repository and check out the intended tested release commit or tag.
Run:

```sh
cp example.env .env
docker compose up -d --build
docker compose ps
docker compose logs -f api
```

The API is available at `http://127.0.0.1:8080` on the server by default.
MongoDB is not published on any host port. Database files persist in the named
`mongo_data` Docker volume. Never use `docker compose down -v` on a live system
unless you deliberately want to delete the database.

## Remote access and security
The MVP API does NOT implement authentication. Do not publish port 8080 to
the Internet or expose this service through a public reverse proxy. Use an
SSH tunnel to reach it while developing, e.g.:

```sh
ssh -L 8080:127.0.0.1:8080 user@your-server
```

For any remote multi-user or public deployment, implement authentication,
authorization and TLS before exposing the API. The MongoDB service runs on
an internal Compose network without database authentication for this local,
single-host deployment; this is **not** a hardened production configuration.
Treat any host Docker access as privileged.

## Operations
- Check status: `docker compose ps`
- Restart: `docker compose restart api`
- Follow logs: `docker compose logs -f --tail=100`
- Update: checkout a vetted tag then `docker compose up -d --build`
- Stop without deleting the database: `docker compose down`
- Backup: stop writes; use a MongoDB-consistent `mongodump` from the mongo
  container to a secure off-host location. Verify restores periodically.

## Resource expectations
The compose file caps API RAM at 512 MiB and MongoDB at 768 MiB. These are
starting limits, not benchmarks or guarantees. Measure under realistic scene
retrieval/write workloads and adjust according to JVM overhead and indexing.
Use a separate VM or larger disk allocation if canon data grows significantly.
