# Knowledge Base Backend

企业知识库系统后端，基于 Spring Boot 3、Java 21 和 Maven 多模块构建。前端位于独立仓库：[knowledge-base-frontend](https://github.com/guojiageng99/knowledge-base-frontend)。

## 模块与端口

| 模块 | 功能 | 端口 | 上下文路径 |
| --- | --- | ---: | --- |
| `kb-gateway` | API 网关 | 8080 | `/` |
| `kb-user-auth` | 注册、登录、认证、用户、团队和权限 | 8081 | `/api/auth` |
| `kb-document` | 文档、评论、收藏、审核、版本和自动保存 | 8082 | `/api/document` |
| `kb-search` | 全文搜索和搜索历史 | 8083 | `/api/search` |
| `kb-file` | 文件上传、对象存储和媒体处理 | 8084 | `/api/file` |
| `kb-ai` | AI 对话、RAG、AI 写作和文档摘要 | 8086 | `/api/ai` |
| `kb-graph` | 知识图谱和 KAG | 8088 | `/api/graph` |
| `kb-foundation` | 字典、系统配置、通知、操作日志和 WebSocket | 8089 | `/api/foundation` |
| `kb-statistics` | 统计中心和数据导出 | 8090 | `/api/statistics` |

`kb-common` 是公共依赖库，不启动 HTTP 服务。

## 环境要求

- JDK 21
- Maven 3.9+
- MySQL 8+
- Redis 6+
- Elasticsearch 8+
- RabbitMQ 3.12+
- MongoDB 6+，用于文档内容和自动保存历史
- Neo4j 5+，用于知识图谱；不使用图谱功能时可暂不启动
- RustFS 或兼容 S3 的对象存储，用于文件存储
- Node.js 20+ 和 npm，用于前端
- FFmpeg/FFprobe，用于媒体转码和缩略图；不使用媒体转码时可关闭对应配置

## 数据库初始化

SQL 文件位于 [`sql`](./sql)。先执行各数据库的初始化脚本，再按文件名顺序执行升级脚本。常用初始化顺序如下：

```text
init_kb_user.sql
init_kb_document.sql
init_kb_file.sql
init_kb_search.sql
init_kb_graph.sql
init_kb_ai.sql
init_kb_foundation.sql
init_kb_category_data.sql
init_kb_tag_data.sql
```

然后执行需要的升级脚本，包括用户、团队、权限、收藏、评论、版本、审核、通知、系统设置、自动保存和统计相关脚本。重复执行前请确认脚本用途；其中升级脚本应优先用于已有数据库。

当前本地 `application.yml` 使用 MySQL `root` 用户和开发密码 `123456`，生产环境必须通过环境变量或外部配置覆盖，不能继续使用默认密码。

## 构建

在后端仓库根目录执行。Windows 本机 JDK 21 路径示例：

```powershell
$env:JAVA_HOME = 'D:\JDK21\package'
mvn -DskipTests clean package
```

只编译 AI 服务及其公共依赖：

```powershell
$env:JAVA_HOME = 'D:\JDK21\package'
mvn -pl kb-ai -am -DskipTests compile
```

## 启动后端

父工程是聚合 POM，没有 Spring Boot 主类，不能直接执行根目录的 `spring-boot:run`。推荐先打包，再分别运行各服务：

```powershell
$env:JAVA_HOME = 'D:\JDK21\package'
mvn -DskipTests package

java -jar kb-user-auth\target\kb-user-auth-1.0.0-SNAPSHOT.jar
java -jar kb-foundation\target\kb-foundation-1.0.0-SNAPSHOT.jar
java -jar kb-document\target\kb-document-1.0.0-SNAPSHOT.jar
java -jar kb-file\target\kb-file-1.0.0-SNAPSHOT.jar
java -jar kb-search\target\kb-search-1.0.0-SNAPSHOT.jar
java -jar kb-graph\target\kb-graph-1.0.0-SNAPSHOT.jar
java -jar kb-statistics\target\kb-statistics-1.0.0-SNAPSHOT.jar
java -jar kb-ai\target\kb-ai-1.0.0-SNAPSHOT.jar
java -jar kb-gateway\target\kb-gateway-1.0.0-SNAPSHOT.jar
```

开发时也可以对单个模块执行：

```powershell
mvn -f kb-user-auth\pom.xml spring-boot:run
```

将 `kb-user-auth` 替换为其他可执行模块即可。启动顺序建议为：

1. MySQL、Redis、Elasticsearch、RabbitMQ、MongoDB、RustFS、Neo4j
2. `kb-user-auth` 和 `kb-foundation`
3. `kb-document`、`kb-file`、`kb-search`、`kb-graph`、`kb-statistics`
4. `kb-ai`
5. `kb-gateway`

网关启动后，前端和第三方客户端优先访问 `http://localhost:8080`；直接访问单个服务时使用上表中的端口和上下文路径。

## 启动前端

```powershell
cd E:\code\knowledge-base-frontend
npm install
npm run dev
```

Vite 开发服务器默认地址为 `http://localhost:5173`。前端开发代理会将 `/api` 请求转发到本地服务，并将 `/ws` 转发到网关。需要修改地址时使用 `VITE_API_BASE_URL`、`VITE_WS_BASE_URL` 等环境变量。

## 主要配置

配置文件位于各模块的 `src/main/resources/application.yml`。常用环境变量包括：

```text
MYSQL_HOST, MYSQL_PORT, MYSQL_USERNAME, MYSQL_PASSWORD
REDIS_HOST, REDIS_PORT, REDIS_PASSWORD
ELASTICSEARCH_URIS
RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_USERNAME, RABBITMQ_PASSWORD
MONGODB_URI
NEO4J_URI, NEO4J_USERNAME, NEO4J_PASSWORD
RUSTFS_ENDPOINT, RUSTFS_PORT, RUSTFS_ACCESS_KEY, RUSTFS_SECRET_KEY, RUSTFS_BUCKET
QWEN_API_KEY, QWEN_BASE_URL, QWEN_MODEL
DEEPSEEK_API_KEY, DEEPSEEK_BASE_URL, DEEPSEEK_MODEL
MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM
APP_INSTANCE_ID
```

`APP_INSTANCE_ID` 用于隔离本地 RabbitMQ 队列和路由键。多套本地环境共用 RabbitMQ 时，为每套环境设置不同的值，例如 `dev-a`、`dev-b`。

AI、邮件、RustFS、Neo4j 和 Elasticsearch 相关功能需要配置对应服务的连接信息和凭据。不要把真实密钥、密码或生产连接串提交到 Git。

## AI 对话

`kb-ai` 提供普通对话和 SSE 流式对话：

```text
POST http://localhost:8086/api/ai/chat
POST http://localhost:8086/api/ai/chat/stream
```

网关地址对应为：

```text
POST http://localhost:8080/api/ai/chat
POST http://localhost:8080/api/ai/chat/stream
```

流式接口发送 `message`、`done` 和 `error` 事件。对话和消息持久化在 `kb_ai.conversation`、`kb_ai.message` 表中，每次请求加载最近 20 条消息作为上下文。

## API 文档

启用 Knife4j 的服务可访问：

```text
http://localhost:<port>/<context-path>/doc.html
```

例如用户认证服务为 `http://localhost:8081/api/auth/doc.html`。通过网关访问时，以网关路由和服务的文档配置为准。

## CodeGraph

本后端仓库已初始化 CodeGraph，索引位于 `.codegraph`。它是本地开发辅助索引，不属于应用运行时数据，也不替代数据库、搜索引擎或知识图谱服务。

## 许可证

本项目用于学习和开发。
