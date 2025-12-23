## Part 2: Build, push, and deploy the app

Goal: build the Java service image, push to a registry, configure manifests, and roll out safely.

### 1) Build and push image
```bash
export REGISTRY=thanhncs              # replace with yours
export IMAGE=$REGISTRY/thanhdev:latest  # or use a commit tag

docker build -t "$IMAGE" .
docker push "$IMAGE"
```

### 2) Update manifests
- Edit `k8s/app.yaml` → set `image: $IMAGE`.
- Keep probes, requests/limits, and Prometheus annotations.
- Apply:
```bash
kubectl apply -f k8s/app.yaml
```

### 3) Image pull secrets (if private registry)
```bash
kubectl -n perf create secret docker-registry regcred \
  --docker-server=$REGISTRY --docker-username=<user> --docker-password=<pwd>
# Patch deployment to use it:
kubectl -n perf patch deploy perf-app -p '{"spec":{"template":{"spec":{"imagePullSecrets":[{"name":"regcred"}]}}}}'
```

### 4) Config and secrets
- Use ConfigMap for non-sensitive settings, Secret for credentials.
- Example (optional):
```yaml
apiVersion: v1
kind: ConfigMap
metadata: {name: perf-app-config, namespace: perf}
data:
  APP_PROFILE: "prod"
---
apiVersion: v1
kind: Secret
metadata: {name: perf-app-secret, namespace: perf}
type: Opaque
stringData:
  DB_PASSWORD: "change-me"
```
- Mount as env:
```yaml
        envFrom:
          - configMapRef: {name: perf-app-config}
          - secretRef: {name: perf-app-secret}
```

### 5) Expose the service
- Already a ClusterIP (`perf-app`). To reach it externally, pick one:
  - Port-forward: `kubectl -n perf port-forward svc/perf-app 18080:80`
  - LoadBalancer Service:
    ```yaml
    kind: Service
    apiVersion: v1
    metadata: {name: perf-app-lb, namespace: perf}
    spec:
      type: LoadBalancer
      selector: {app: perf-app}
      ports:
        - port: 80
          targetPort: 8080
    ```
  - Ingress (needs ingress controller):
    ```yaml
    apiVersion: networking.k8s.io/v1
    kind: Ingress
    metadata: {name: perf-app, namespace: perf}
    spec:
      rules:
        - host: perf.example.com
          http:
            paths:
              - path: /
                pathType: Prefix
                backend:
                  service:
                    name: perf-app
                    port: {number: 80}
    ```

### 6) Rollouts and verification
- Start rollout: `kubectl -n perf rollout restart deploy/perf-app`
- Status: `kubectl -n perf rollout status deploy/perf-app`
- Describe: `kubectl -n perf describe deploy/perf-app`
- Pods: `kubectl -n perf get pods -l app=perf-app -o wide`
- Logs (newest pod): `kubectl -n perf logs -l app=perf-app --max-log-requests=1`
- Quick check: `kubectl -n perf port-forward svc/perf-app 18080:80 && curl "http://localhost:18080/work?n=20000"`

### 7) Rollback
```bash
kubectl -n perf rollout undo deploy/perf-app
```

### 8) Promote with tags
- Build with commit tag: `IMAGE=$REGISTRY/thanhdev:$(git rev-parse --short HEAD)`
- Update manifest via kustomize/Helm or `kubectl set image deploy/perf-app perf-app=$IMAGE`
- Keep `latest` only for dev; use immutable tags for staging/prod.

Next: see Part 3 for observability (Prometheus/Grafana/logs/traces).
