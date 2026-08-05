# Knowledge Base Backend

企业知识库系统后端，基于 Spring Boot、Java 21 和 Maven 多模块构建。前端项目位于独立仓库：

- [knowledge-base-frontend](https://github.com/guojiageng99/knowledge-base-frontend)

## Modules

| Module | Purpose | Port | Context path |
| --- | --- | ---: | --- |
| `kb-gateway` | API gateway | 8080 | `/` |
| `kb-user-auth` | User registration, login and authentication | 8081 | `/api/auth` |
| `kb-document` | Document management | 8082 | `/api/document` |
| `kb-search` | Full-text and vector search | 8083 | `/api/search` |
| `kb-file` | File storage and processing | 8084 | `/api/file` |
| `kb-ai` | AI chat, RAG and writing assistance | 8086 | `/api/ai` |
| `kb-graph` | Knowledge graph and KAG | 8088 | `/api/graph` |
| `kb-foundation` | Dictionary, notification and operation-log services | 8089 | `/api/foundation` |
| `kb-statistics` | Statistics and dashboard data | 8090 | `/api/statistics` |

`kb-common` is the shared library module and does not start an HTTP service.

## Requirements

- JDK 21
- Maven 3.9+
- MySQL 8+
- Redis 6+
- Elasticsearch 8+
- RabbitMQ 3.12+
- Node.js 20+ and npm for the frontend

Some modules can start with optional integrations disabled, but the corresponding database and middleware must be available when the module configuration enables them.

## Build

Set `JAVA_HOME` to JDK 21, then run from the repository root:

```powershell
$env:JAVA_HOME = 'D:\JDK21\package'
mvn -DskipTests clean package
```

Compile only the AI service and its dependencies:

```powershell
$env:JAVA_HOME = 'D:\JDK21\package'
mvn -pl kb-ai -am -DskipTests compile
```

## Start Services

The parent project is an aggregator POM and does not have a main class. Start each executable service from its own module. For example:

```powershell
$env:JAVA_HOME = 'D:\JDK21\package'
mvn -pl kb-user-auth -am spring-boot:run
```

Other services use the same pattern, replacing `kb-user-auth` with the module name. A typical local startup order is:

1. MySQL, Redis, Elasticsearch, RabbitMQ
2. `kb-user-auth`
3. `kb-foundation`
4. `kb-document`, `kb-file`, `kb-search`, `kb-graph`, `kb-statistics`
5. `kb-ai`
6. `kb-gateway`

The frontend can then be started in the separate repository:

```powershell
cd E:\code\knowledge-base-frontend
npm install
npm run dev
```

## Database

SQL scripts are under [`sql`](./sql). The exact database name and credentials are controlled by each module's `application.yml` and environment variables. The default local MySQL password in the development configuration is `123456`; change it for any shared or production environment.

## Configuration

Module configuration files are under each module's `src/main/resources/application.yml`. Common environment variables include:

```text
MYSQL_HOST, MYSQL_PORT, MYSQL_USERNAME, MYSQL_PASSWORD
REDIS_HOST, REDIS_PORT, REDIS_PASSWORD
ELASTICSEARCH_URIS
RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_USERNAME, RABBITMQ_PASSWORD
QWEN_API_KEY, QWEN_BASE_URL, QWEN_MODEL
DEEPSEEK_API_KEY, DEEPSEEK_BASE_URL, DEEPSEEK_MODEL
AI_DEFAULT_MODEL
```

Do not commit real API keys, passwords or production connection strings.

## AI Chat

`kb-ai` provides normal and SSE streaming chat endpoints:

```text
POST http://localhost:8086/api/ai/chat
POST http://localhost:8086/api/ai/chat/stream
```

Streaming chat sends `message`, `done` and `error` SSE events. Conversation messages are persisted in the `conversation` and `message` tables, and the latest 20 messages are loaded as the chat history for subsequent requests.

## CodeGraph

This backend repository has been initialized with CodeGraph. The current index is stored in `.codegraph` and includes Java and YAML source files. CodeGraph is a local development index and should not be treated as application runtime data.

## License

This project is for learning and development purposes.
