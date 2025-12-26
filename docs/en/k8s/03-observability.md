## Part 3: Observability (metrics, logs, traces)

Goal: instrument and see the app in Prometheus/Grafana, collect logs, and (optionally) traces.

### 1) Metrics (Prometheus/Grafana)
- App already exposes Micrometer at `/actuator/prometheus`.
- Service annotation in `k8s/app.yaml`:
```yaml
metadata:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/path: "/actuator/prometheus"
    prometheus.io/port: "8080"
```
- Install kube-prometheus-stack (staging/prod-like):
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm upgrade --install kube-prometheus-stack prometheus-community/kube-prometheus-stack \
  --namespace monitoring --create-namespace
kubectl -n monitoring port-forward svc/kube-prometheus-stack-grafana 3000:80
# Grafana: http://localhost:3000 (default admin/prom-operator, change it)
```
- Useful dashboards:
  - Kubernetes / Compute Resources / Pod
  - JVM (Micrometer) dashboards
  - HTTP server metrics (Micrometer)

### 2) Logs
- Quick: `kubectl -n perf logs -l app=perf-app --tail=200`
- Centralized options:
  - Loki stack: `helm upgrade --install loki grafana/loki-stack -n monitoring`
  - EFK: fluent-bit → Elasticsearch/OpenSearch → Kibana
- Add correlation IDs in app logs if possible (via servlet filter or WebMvc config).

### 3) Traces (optional but recommended)
- Choose Otel Collector or Tempo/Jaeger.
- Minimal Otel Collector example (values to adjust):
```yaml
receivers:
  otlp:
    protocols: {http: {}, grpc: {}}
exporters:
  logging: {}
  tempo: {endpoint: http://tempo:4317}  # example
service:
  pipelines:
    traces:
      receivers: [otlp]
      exporters: [tempo, logging]
```
- Instrument app with OpenTelemetry Java agent:
  - Add to image and start with `-javaagent:/otel/javaagent.jar`
  - Set env: `OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317`

### 4) Alert seeds (starting points)
- Availability: `http_server_requests_seconds_count{status!~"5.."} < threshold`
- Latency: P95 `http_server_requests_seconds_bucket{}` or `histogram_quantile`
- Error rate: 5xx rate over 5m
- JVM: high CPU, heap > 85%, GC pause > threshold
- Pod health: restarts > 3 in 10m, readiness failures

### 5) Fast checks during debugging
```bash
kubectl -n perf top pods
kubectl -n perf describe pod <pod>
kubectl -n perf logs <pod>
kubectl -n monitoring port-forward svc/kube-prometheus-stack-prometheus 9090:9090
kubectl -n monitoring port-forward svc/kube-prometheus-stack-grafana 3000:80
```

Next: Part 4 covers scaling and resilience (HPA, rollouts, disruption budgets).
