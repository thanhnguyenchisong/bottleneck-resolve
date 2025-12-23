 #### Giai đoạn phân tích chi tiết:
 
    Chỉ đẩy workload vào 1 Pod → attach profiler → tìm bottleneck trong code.
 #### Giai đoạn mô phỏng production:
    Đẩy workload qua service (load balancer) để phân phối đều cho nhiều Pod.

    Dùng metrics/tracing để phát hiện Pod nào nóng → attach profiler vào Pod đó.
## Production-style bottleneck resolution on Kubernetes

This guide extends the local workflow (JMeter + Async Profiler + Prometheus/Grafana) to a Kubernetes environment so you can reproduce, detect, and mitigate bottlenecks in production or prod-like clusters.

### 1) Prerequisites

- A container registry you can push to (replace `REGISTRY` below).
- `kubectl` configured for the target cluster; cluster has load-generator egress to the app.
- Helm (for Prometheus/Grafana) and JMeter installed locally, or use a JMeter runner pod.
- Async Profiler bundle available locally (will be copied into the pod when needed).

### 2) Build and publish the image

```bash
export REGISTRY=my-registry.example.com/myteam
export APP_IMG=$REGISTRY/performance-improve:$(git rev-parse --short HEAD)

docker build -t "$APP_IMG" .
docker push "$APP_IMG"
```

### 3) Kubernetes manifests (Deployment + Service)

Save as `k8s/app.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: perf-app
  namespace: perf
spec:
  replicas: 3
  selector:
    matchLabels:
      app: perf-app
  template:
    metadata:
      labels:
        app: perf-app
    spec:
      containers:
        - name: perf-app
          image: REGISTRY/performance-improve:TAG
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
          env:
            - name: JAVA_TOOL_OPTIONS
              value: "-XX:+UseG1GC"
          resources:
            requests:
              cpu: "500m"
              memory: "512Mi"
            limits:
              cpu: "1"
              memory: "1Gi"
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: perf-app
  namespace: perf
spec:
  selector:
    app: perf-app
  ports:
    - name: http
      port: 80
      targetPort: 8080
```

Deploy:

```bash
kubectl create namespace perf
kubectl apply -f k8s/app.yaml
```

Replace `REGISTRY/performance-improve:TAG` before applying, or template with kustomize/Helm.

### 4) Observability stack (Prometheus + Grafana)

Option A: install community chart in the same cluster (suitable for staging/prod-like):

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm upgrade --install kube-prometheus-stack prometheus-community/kube-prometheus-stack \
  --namespace monitoring --create-namespace
```

Expose Grafana locally:

```bash
kubectl -n monitoring port-forward svc/kube-prometheus-stack-grafana 3000:80
# Grafana: http://localhost:3000 (admin/prom-operator by default; change it)
```

Prometheus will scrape `/actuator/prometheus` if the `Service` has the right annotations. Add to `perf-app` service if needed:

```yaml
metadata:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/path: "/actuator/prometheus"
    prometheus.io/port: "8080"
```

### 5) Load generation with JMeter in-cluster (headless)

Create a simple runner pod so traffic stays inside the cluster/VPC (lower latency and more realistic network):

```bash
kubectl -n perf run jmeter --image=justb4/jmeter:5.6.3 --restart=Never --command -- sleep 3600
kubectl -n perf cp work-load.jmx jmeter:/tmp/work-load.jmx
kubectl -n perf exec -it jmeter -- jmeter -n -t /tmp/work-load.jmx \
  -JserverHost=perf-app.perf.svc.cluster.local \
  -JserverPort=80 \
  -Jn=20000 \
  -l /tmp/result.jtl -e -o /tmp/report
kubectl -n perf cp jmeter:/tmp/report ./report
```

You can also run JMeter locally against the cluster ingress if cross-VPC latency is acceptable.

### 6) Async Profiler in Kubernetes (options and commands)

Pick the option that best fits your access model and change-control rules.

**A) Copy profiler into a running pod (no image change)**

- One-time per rollout:

```bash
POD=$(kubectl -n perf get pod -l app=perf-app -o jsonpath='{.items[0].metadata.name}')
kubectl -n perf cp /path/to/async-profiler "$POD":/tmp/async-profiler
```

- Find PID and profile (30s CPU):

```bash
kubectl -n perf exec -it "$POD" -- jcmd | grep performance-improve
kubectl -n perf exec "$POD" -- /tmp/async-profiler/profiler.sh \
  -d 30 -e cpu -f /tmp/flame.svg <PID>
kubectl -n perf cp "$POD":/tmp/flame.svg ./flame.svg
kubectl -n perf exec "$POD" -- rm /tmp/flame.svg
```

**B) Bake profiler into the app image**

- Add the bundle under `/opt/async-profiler` in `Dockerfile`.
- Run without copying:

```bash
kubectl -n perf exec "$POD" -- /opt/async-profiler/profiler.sh \
  -d 30 -e cpu -f /tmp/flame.svg <PID>
```

**C) Ephemeral container (keeps main container untouched; K8s ≥1.23)**

```bash
kubectl -n perf debug pod/"$POD" -c ap \
  --image=your-profiler-image --target=perf-app
# Inside ephemeral container:
jcmd | grep performance-improve
/opt/async-profiler/profiler.sh -d 30 -e cpu -f /tmp/flame.svg <PID>
# Copy out from the main pod name:
kubectl -n perf cp "$POD":/tmp/flame.svg ./flame.svg
```

**D) Sidecar with process namespace sharing (repeatable)**

- Set `shareProcessNamespace: true` on the pod spec.
- Run profiler from the sidecar against the app PID (same commands as above).
- Good for recurring profiling without touching the main container.

**E) Helper pod + kubectl cp/exec**

- Run a toolbox pod (e.g., `busybox`/`ubuntu`) in the same namespace.
- Use it to copy the profiler into the target pod and exec the commands (same as option A) without installing tooling locally.

**F) Host-level attach (privileged/ops-only)**

- From the node (SSH or privileged DaemonSet), find the container PID via `crictl ps` or `docker ps` + `nsenter`.
- Run `profiler.sh` from the host into the container PID. Use only if `kubectl exec` is restricted.

**Event types and knobs**

- `-e cpu` (default, lowest overhead), `-e wall` (blocking/locks/I/O), `-e alloc` (alloc hotspots; higher overhead).
- Duration: 15–60s typical; shorter in prod.
- Thread states: add `-t` with `-e wall` for blocking detail.
- Output: `-f /tmp/flame.svg` (SVG), or `-o collapsed` for text stacks.

**Correlate with metrics and load**

- Align profile window with Grafana spike times.
- If safe, trigger load (JMeter in-cluster) during the window for clean signals.
- Profile one pod at a time to reduce impact.

### 7) Bottleneck investigation workflow (prod-style)

1. **Detect:** Watch Grafana dashboards (HTTP latency, throughput, JVM CPU, GC). Note the exact spike window.
2. **Reproduce with load:** Run JMeter against the service/ingress with the same parameters that triggered the issue.
3. **Capture profile:** Trigger Async Profiler during the spike window; export `flame.svg`.
4. **Analyze:** Identify hot methods or blocking stacks; correlate with Prometheus timings.
5. **Mitigate:** Apply code fixes, JVM flags, or K8s changes (e.g., increase replicas, tune requests/limits, bump heap if GC bound).
6. **Verify:** Roll out a new image, rerun JMeter, and compare Grafana + flame graph improvements.
7. **Record:** Keep before/after metrics and flame graphs in your incident runbook.

### 8) Operational tips

- Keep requests/limits realistic; under-allocating CPU can hide code regressions behind throttling.
- Use HPA (CPU or custom metrics) after you understand the steady-state baseline.
- Prefer profiling one pod at a time to minimize impact; avoid during peak unless necessary.
- Clean up profiler artifacts from pods: `kubectl -n perf exec "$POD" -- rm /tmp/flame.svg`.
- Store generated JTL and reports in an artifact bucket for audits.
