# Tài liệu Backend

Tài liệu luyện phỏng vấn và tham khảo cho lập trình backend: Java, framework, database, message queue, SSO. Mỗi folder có README riêng với mục lục và thứ tự đọc.

## 🎯 Mục tiêu Master Backend

**Học thuộc hết** nội dung trong toàn bộ folder backend và trả lời được **Checklist Master Backend** → **hoàn toàn tự tin pass phỏng vấn master backend**.

→ **[MASTER-BACKEND-CHECKLIST.md](./MASTER-BACKEND-CHECKLIST.md)** — tổng hợp câu hỏi phỏng vấn theo chủ đề (Java, JVM, Concurrency, Spring, JPA/DB, REST, Microservices, Kafka, Security, System Design) và cách ôn. Làm xong checklist = sẵn sàng master.

---

## Cấu trúc

| Folder | Mô tả |
|--------|--------|
| [**java**](./java/) | Java 8–21, OOP, Collections, Concurrency, JVM, Spring, REST API |
| [**jpa**](./jpa/) | JPA, Entity, Queries, Spring Data JPA, Transactions |
| [**spring-jpa**](./spring-jpa/) | Spring Data JPA: Repository, query methods, custom queries |
| [**relational-database**](./relational-database/) | SQL, thiết kế DB, index, ACID, administration |
| [**postgresSQL**](./postgresSQL/) | PostgreSQL: types, performance, backup, security |
| [**kafka**](./kafka/) | Kafka: topics, producers, consumers, Streams |
| [**rabbitMQ**](./rabbitMQ/) | RabbitMQ: exchanges, queues, Spring AMQP |
| [**redis**](./redis/) | Redis: data structures, cache patterns, Spring Data Redis, Cluster, Sentinel |
| [**mongodb**](./mongodb/) | MongoDB: document model, queries, aggregation, Spring Data, replica set, sharding |
| [**sql**](./sql/) | SQL: SELECT, JOIN, subquery, CTE, aggregation, optimization, index |
| [**jfrog**](./jfrog/) | JFrog Artifactory: artifact repository, Maven/npm/Docker, CI/CD, Xray |
| [**harbor**](./harbor/) | Harbor: container registry, replication, scan CVE, RBAC, CI/CD |
| [**microservices**](./microservices/) | Microservices: communication, discovery, gateway, patterns |
| [**quakus**](./quakus/) | Quarkus: REST, DI, reactive, native image |
| [**maven**](./maven/) | Maven: POM, lifecycle, dependencies, multi-module |
| [**sso**](./sso/) | SSO: SAML, OAuth2, OIDC, JWT |

## Lộ trình gợi ý

- **Bắt đầu**: [java](./java/) → [maven](./maven/) → [relational-database](./relational-database/) hoặc [postgresSQL](./postgresSQL/)
- **Spring**: [java](./java/) (Spring phần) → [spring-jpa](./spring-jpa/) → [jpa](./jpa/)
- **Message & scale**: [kafka](./kafka/) hoặc [rabbitMQ](./rabbitMQ/) → [microservices](./microservices/)
- **Cache**: [redis](./redis/) — cache, session, rate limit, Spring Data Redis
- **NoSQL**: [mongodb](./mongodb/) — document DB, aggregation, Spring Data MongoDB
- **SQL**: [sql](./sql/) — truy vấn, JOIN, CTE, optimization (bổ sung [relational-database](./relational-database/))
- **Artifact & Registry**: [jfrog](./jfrog/) (Artifactory), [harbor](./harbor/) (container registry) — CI/CD, build, deploy
- **Auth**: [sso](./sso/)
- **Master**: Học hết các folder trên → làm **[MASTER-BACKEND-CHECKLIST.md](./MASTER-BACKEND-CHECKLIST.md)** để tự kiểm tra và ôn system design/scalability.

Đọc README trong từng folder để xem mục lục chi tiết và bài tập.
