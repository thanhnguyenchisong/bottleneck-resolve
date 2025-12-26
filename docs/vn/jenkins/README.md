## Hướng dẫn nhanh Jenkins từ cơ bản đến pro

Theo dõi các phần theo thứ tự:

1. `01-basics.md` — Jenkins là gì, tùy chọn cài đặt, khái niệm cốt lõi, UI tour.
2. `02-pipelines.md` — Declarative vs Scripted pipelines, examples Jenkinsfile, shared libs.
3. `03-ci-cd-practices.md` — agents, credentials, quality gates, caching, security/hardening.

### File mẫu và giải thích chi tiết

- **`Jenkinsfile`** — File Jenkinsfile mẫu production-ready cho Spring Boot application
  - Bao gồm: build, test, security scan, Docker build, deployment
  - Sử dụng Declarative Pipeline syntax (khuyến nghị)
  - Có thể copy trực tiếp vào root project và tùy chỉnh
  
- **`Jenkinsfile-explanation.md`** — Giải thích chi tiết từng phần trong file Jenkinsfile
  - Giải thích từng section, keyword, và best practices
  - Có ví dụ, bảng giải thích, và FAQ
  - Bao gồm: agent config, options, environment variables, stages, post actions, credentials

Sử dụng file `Jenkinsfile` mẫu và `docker run jenkins/jenkins:lts` hoặc local server trong khi đọc để thực hành.