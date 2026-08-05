# Knowledge Base Docker Deployment

This directory runs the knowledge base with Docker Compose. It uses the JAR files built with JDK 21 and the frontend `dist` output.

## Prepare the deployment directory

From the backend repository root, copy the service JARs to `deploy/docker/backend/` using these names:

```text
kb-gateway.jar
kb-user-auth.jar
kb-document.jar
kb-search.jar
kb-file.jar
kb-ai.jar
kb-graph.jar
kb-statistics.jar
kb-foundation.jar
```

Copy the frontend build output to `deploy/docker/frontend-dist/`.

Copy the numbered SQL initialization files to `deploy/docker/mysql/init/`. Do not copy the large `knowledge_base_export_*.sql` dump into this directory unless intentionally restoring that dump.

## Start

```bash
cp .env.example .env
chmod 600 .env
docker compose config
docker compose up -d
```

The default profile starts MySQL, Redis, MongoDB, RustFS, the core backend services, and the frontend. The `full` profile additionally starts Elasticsearch, Neo4j, search, AI, and statistics:

```bash
docker compose --profile full up -d
```

The frontend is available on port 80. The gateway and internal services are not published directly to the host.

## Operations

```bash
docker compose ps
docker compose logs -f gateway
docker compose logs --tail=200 user-auth
docker compose down
```

Data is stored in named Docker volumes. `docker compose down` does not remove those volumes.
