## Prometheus + Grafana (Micrometer) setup

### 1. Build Docker image

```bash
mvn clean package -DskipTests
docker compose build app
```

### 2. Start the stack

```bash
docker compose up
```

Services:

- **App**: `http://localhost:8080` (metrics at `/actuator/prometheus`)
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000` (user `admin` / `admin`)

### 3. Configure Grafana

1. Add data source: **Prometheus** at `http://prometheus:9090`.
2. Create a dashboard with panels like:
   - `rate(http_server_requests_seconds_count[1m])` for request rate.
   - `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))` for P95 latency.
   - `process_cpu_usage`, `jvm_memory_used_bytes` for JVM health.

### 4. Generate load and observe

Use JMeter (see `docs/03-jmeter-load-test.md`) to hit `/work` with high concurrency and monitor:

- Increased latency and error rates.
- CPU and memory behaviours.
