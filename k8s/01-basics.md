## Part 1: Kubernetes basics (crash start)

### Mental model
- **Node** runs **Pods**; each Pod has 1+ containers sharing network + volumes.
- **Deployment** owns ReplicaSets, keeps desired replica count.
- **Service** gives stable virtual IP/DNS to Pods; **ClusterIP** for internal, **LoadBalancer/Ingress** for external.
- **Requests/Limits** control scheduling and throttling; right-sizing matters for performance tests.
- **ConfigMap/Secret** inject config; **Namespace** partitions resources.

### Tooling
- `kubectl` (CLI), `kubectx`/`kubens` optional, `kubectl krew top` for plugins.
- Access kubeconfig; set default namespace: `kubectl config set-context --current --namespace=perf`.

### Quickstart commands
```bash
# Get cluster + node info
kubectl cluster-info
kubectl get nodes -o wide

# Inspect workloads
kubectl get ns
kubectl get pods -n perf -o wide
kubectl describe pod <pod> -n perf
kubectl logs <pod> -n perf

# Port-forward to a pod or service
kubectl -n perf port-forward pod/<pod> 18080:8080
kubectl -n perf port-forward svc/perf-app 18080:80

# Exec into a container
kubectl -n perf exec -it <pod> -- /bin/sh

# Apply/delete manifests
kubectl apply -f k8s/app.yaml
kubectl delete -f k8s/app.yaml

# Top (requires metrics-server)
kubectl top pods -n perf
kubectl top nodes
```

### Minimal local loop
1) Build & push image: see `02-deploy-app.md`.
2) Deploy: `kubectl apply -f k8s/app.yaml`.
3) Smoke test: `kubectl -n perf port-forward svc/perf-app 18080:80 && curl "http://localhost:18080/work?n=20000"`.
4) Check logs + describe pod if failing.

### Anatomy of the app manifest (k8s/app.yaml)
- Namespace `perf`
- Deployment `perf-app` (3 replicas, probes, requests/limits)
- Service `perf-app` (port 80 → pod 8080) with Prometheus scrape annotations

Move to Part 2 to build, push, and roll out images safely.
