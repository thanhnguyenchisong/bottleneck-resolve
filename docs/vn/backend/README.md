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
| [**microservices**](./microservices/) | Microservices: communication, discovery, gateway, patterns |
| [**quakus**](./quakus/) | Quarkus: REST, DI, reactive, native image |
| [**maven**](./maven/) | Maven: POM, lifecycle, dependencies, multi-module |
| [**sso**](./sso/) | SSO: SAML, OAuth2, OIDC, JWT |

## Lộ trình gợi ý

- **Bắt đầu**: [java](./java/) → [maven](./maven/) → [relational-database](./relational-database/) hoặc [postgresSQL](./postgresSQL/)
- **Spring**: [java](./java/) (Spring phần) → [spring-jpa](./spring-jpa/) → [jpa](./jpa/)
- **Message & scale**: [kafka](./kafka/) hoặc [rabbitMQ](./rabbitMQ/) → [microservices](./microservices/)
- **Auth**: [sso](./sso/)
- **Master**: Học hết các folder trên → làm **[MASTER-BACKEND-CHECKLIST.md](./MASTER-BACKEND-CHECKLIST.md)** để tự kiểm tra và ôn system design/scalability.

Đọc README trong từng folder để xem mục lục chi tiết và bài tập.
