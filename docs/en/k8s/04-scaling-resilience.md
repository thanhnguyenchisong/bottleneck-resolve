## Part 4: Scaling and resilience

Goal: keep the service responsive under load and resilient to failures/maintenance.

### 1) Requests/limits (avoid throttling, ensure placement)
- Start with: requests `cpu: 500m`, `memory: 512Mi`; limits `cpu: 1`, `memory: 1Gi` (see `k8s/app.yaml`).
- Tune using `kubectl top pods` and Grafana CPU/memory panels.
- Avoid setting CPU limits too low (can cause throttling); keep request ≈ typical steady-state.

### 2) Readiness/Liveness probes
- Already present on `/actuator/health`.
- Keep readiness conservative so Pods enter endpoints only when ready; liveness for stuck states.

### 3) Horizontal Pod Autoscaler (HPA)
Example (CPU-based):
```bash
kubectl -n perf autoscale deploy perf-app --cpu-percent=70 --min=3 --max=10
```
YAML version:
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata: {name: perf-app, namespace: perf}
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: perf-app
  minReplicas: 3
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```
- For RPS/latency-based HPA, expose custom metrics via Prometheus Adapter.

### 4) Rollout strategy
- Use rolling update with small surge and zero unavailable, e.g.:
```yaml
spec:
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 0
      maxSurge: 1
```
- For risky releases, do canary (e.g., Argo Rollouts) or manual staged rollout by scaling a second Deployment.

### 5) Disruption and availability
- PodDisruptionBudget:
```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata: {name: perf-app-pdb, namespace: perf}
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: perf-app
```
- Topology spread (keep replicas across nodes/Zones):
```yaml
topologySpreadConstraints:
  - maxSkew: 1
    topologyKey: kubernetes.io/hostname
    whenUnsatisfiable: DoNotSchedule
    labelSelector:
      matchLabels:
        app: perf-app
```
- Anti-affinity to avoid same-node packing (optional):
```yaml
affinity:
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      - labelSelector:
          matchExpressions:
            - key: app
              operator: In
              values: [perf-app]
        topologyKey: kubernetes.io/hostname
```

### 6) Ingress/traffic patterns
- Prefer Ingress/LoadBalancer for user traffic; port-forward only for debugging.
- Common patterns:
  - **ClusterIP + Ingress**: Ingress terminates TLS and routes to `perf-app` service (recommended).
  - **LoadBalancer service**: simplest external exposure when an ingress controller is absent.
  - **Internal-only**: keep ClusterIP and use port-forward or private ingress class.
- NGINX Ingress example with TLS and path prefix:
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: perf-app
  namespace: perf
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$1
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  ingressClassName: nginx
  tls:
    - hosts: [perf.example.com]
      secretName: perf-tls
  rules:
    - host: perf.example.com
      http:
        paths:
          - path: /(.*)
            pathType: Prefix
            backend:
              service:
                name: perf-app
                port:
                  number: 80
```
- Canary via Ingress (concepts):
  - Duplicate service (e.g., `perf-app-canary`) and use weighted routing (available in some ingress controllers like NGINX with annotations or Istio/Traefik/NGINX Plus).
  - Alternatively, run two Deployments behind one Service and control traffic with pod weights (service meshes) or by scaling canary replicas small.
- Sticky sessions: avoid for stateless services; if needed, NGINX cookie affinity: `nginx.ingress.kubernetes.io/affinity: "cookie"`.
- Rate limiting (edge protection): `nginx.ingress.kubernetes.io/limit-rps: "100"` (per client IP). Use WAF/edge for stronger controls.
- Headers to preserve client info: ensure ingress sets `X-Forwarded-For`, `X-Forwarded-Proto`; Spring Boot can honor these for correct scheme/redirects.

### 7) Chaos and failure drills (optional but valuable)
- Pod kill: `kubectl -n perf delete pod -l app=perf-app` (rollout should heal).
- Node drain simulation: `kubectl drain <node> --ignore-daemonsets --delete-emptydir-data` (ensure PDB respected).
- Check that HPA scales back down and that Prometheus/Grafana stay healthy.

Next: Part 5 focuses on performance testing and profiling on K8s.
