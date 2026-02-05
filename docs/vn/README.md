# Tài liệu tiếng Việt

Tài liệu được tổ chức theo bốn nhóm: **Web** (CSS, SCSS, Responsive, Reactive, A11y, Performance, Security), **Frontend** (Angular, TypeScript, UI), **Backend** (phát triển ứng dụng, ngôn ngữ, database, auth) và **DevOps** (CI/CD, hạ tầng, observability).

---

## Cấu trúc tài liệu

```
docs/vn/
├── README.md          ← Bạn đang ở đây
├── web/               ← CSS, SCSS, Responsive, Reactive, A11y, Performance, Security
│   ├── 01-css-fundamentals.md … 10-senior-web-checklist.md
│   └── README.md
├── frontend/          ← Angular & ứng dụng web hoàn chỉnh
│   ├── 01-typescript-basics.md … 15-master-angular.md
│   └── README.md
├── backend/           ← Lập trình & công nghệ backend
│   ├── java/          ← Java, OOP, Spring, JVM
│   ├── jpa/           ← JPA, Hibernate
│   ├── spring-jpa/    ← Spring Data JPA
│   ├── relational-database/
│   ├── kafka/
│   ├── rabbitMQ/
│   ├── microservices/
│   ├── quakus/        ← Quarkus
│   ├── maven/
│   ├── postgresSQL/
│   └── sso/
└── devops/            ← CI/CD, hạ tầng, vận hành
    ├── git/
    ├── gitlab/
    ├── jenkins/
    ├── k8s/
    ├── k8s-udemy/
    ├── helm/
    ├── terraform/
    └── bottleneck-resolve/   ← Profiling, JMeter, Prometheus/Grafana
```

---

## Web

Tài liệu **nền tảng web** (không phụ thuộc framework): **CSS**, **SCSS**, **Responsive Web Design**, **Reactive Programming**, **Accessibility**, **Performance**, **Browser/DOM**, **Security**. Có **Checklist Senior Web** (bài 10) để tự kiểm tra — học xong kết hợp với Frontend (Angular) để **đi phỏng vấn senior pass ngay**.

| # | Nội dung |
|---|----------|
| 01–02 | CSS Fundamentals, Layout (Flexbox & Grid) |
| 03–04 | SCSS/Sass, Responsive Web Design |
| 05–07 | Reactive Programming, Accessibility, Web Performance |
| 08–09 | Browser/DOM/Event Loop, Web Security |
| 10 | **Checklist Senior Web** — câu hỏi phỏng vấn |

→ Xem [web/README.md](frontend/web/README.md) để có mục lục chi tiết và lộ trình đọc.

---

## Frontend

Tài liệu **Angular** và các thành phần xung quanh để build một **ứng dụng web Angular hoàn chỉnh**: TypeScript, components, routing, forms, HTTP, RxJS, state, UI (Material), testing, build & deploy.

| Nội dung | File |
|----------|------|
| TypeScript, Angular căn bản, Components & Templates | [01–03](./frontend/) |
| Directives, Pipes, Services & DI, Routing | [04–06](./frontend/) |
| Forms, HTTP Client, RxJS trong Angular | [07–09](./frontend/) |
| State & kiến trúc, UI & Styling, Testing, Build & Deploy | [10–13](./frontend/) |

→ Xem [frontend/README.md](./frontend/README.md) để có mục lục chi tiết và lộ trình đọc.

---

## Backend

Tài liệu luyện phỏng vấn và tham khảo cho lập trình backend: Java, framework, database, message queue, SSO.

| Folder | Nội dung |
|--------|----------|
| [**backend/java**](./backend/java/) | Java 8–21, OOP, Collections, Concurrency, JVM, Spring, REST API |
| [**backend/jpa**](./backend/jpa/) | JPA, Entity, Queries, Spring Data JPA, Transactions |
| [**backend/spring-jpa**](./backend/spring-jpa/) | Spring Data JPA: Repository, Query methods, custom queries |
| [**backend/relational-database**](./backend/relational-database/) | SQL, thiết kế DB, index, ACID, administration |
| [**backend/postgresSQL**](./backend/postgresSQL/) | PostgreSQL: types, performance, backup, security |
| [**backend/kafka**](./backend/kafka/) | Kafka: topics, producers, consumers, Streams |
| [**backend/rabbitMQ**](./backend/rabbitMQ/) | RabbitMQ: exchanges, queues, Spring AMQP |
| [**backend/microservices**](./backend/microservices/) | Microservices: communication, discovery, gateway, patterns |
| [**backend/quakus**](./backend/quakus/) | Quarkus: REST, DI, reactive, native image |
| [**backend/maven**](./backend/maven/) | Maven: POM, lifecycle, dependencies, multi-module |
| [**backend/sso**](./backend/sso/) | SSO: SAML, OAuth2, OIDC, JWT |

→ Xem [backend/README.md](./backend/README.md) để có mục lục chi tiết và lộ trình đọc.

---

## DevOps

Tài liệu CI/CD, container, Kubernetes, IaC và điều tra hiệu suất.

| Folder | Nội dung |
|--------|----------|
| [**devops/git**](./devops/git/) | Git: fundamentals, branching, remote, workflow, troubleshooting |
| [**devops/gitlab**](./devops/gitlab/) | GitLab CI/CD: pipelines, `.gitlab-ci.yml`, file mẫu |
| [**devops/jenkins**](./devops/jenkins/) | Jenkins: pipelines, Jenkinsfile mẫu, so sánh với GitLab |
| [**devops/k8s**](./devops/k8s/) | Kubernetes: deploy app, observability, scaling, profiling trên K8s |
| [**devops/k8s-udemy**](./devops/k8s-udemy/) | Kubernetes chi tiết: manifest, networking, security, Kustomize |
| [**devops/helm**](./devops/helm/) | Helm: chart, values, templating, release |
| [**devops/terraform**](./devops/terraform/) | Terraform: state, modules, testing, security |
| [**devops/bottleneck-resolve**](./devops/bottleneck-resolve/) | **Demo app**: JMeter, Async Profiler, Prometheus/Grafana, hướng dẫn tìm điểm nghẽn |

→ Mỗi folder có `README.md` với thứ tự đọc và mô tả ngắn.

---

## Demo: Cải thiện hiệu suất (bottleneck-resolve)

Ứng dụng Spring Boot dùng để học **kiểm thử tải** và **phân tích điểm nghẽn**:

- **Tạo tải**: JMeter  
- **Phân tích CPU**: Async Profiler (flame graph)  
- **Số liệu**: Micrometer → Prometheus → Grafana  

Endpoint mẫu: `GET /work?n=10000` (thuật toán cố ý chậm). Hướng dẫn từng bước nằm trong [devops/bottleneck-resolve](./devops/bottleneck-resolve/).
