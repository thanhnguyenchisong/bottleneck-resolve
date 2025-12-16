## Performance Improve Demo

This project is a small Spring Boot application used to learn **performance testing** and **bottleneck analysis**.

### Tech stack

- **Load generation (local)**: JMeter
- **Profiling (local)**: Async Profiler
- **Metrics (prod-like)**: Micrometer → Prometheus → Grafana

### Scenario

- REST endpoint `GET /work?n=10000` with an **intentionally inefficient** algorithm (quadratic complexity using `List.contains`).
- Use **JMeter** to send concurrent requests and observe high latency / CPU usage.
- Use **Async Profiler** to capture a CPU flame graph and identify the hot method.
- Use **Micrometer + Prometheus + Grafana** to monitor request latency, throughput, and JVM metrics in a prod-like setup.

More detailed step-by-step guides will be added in `docs/`.
