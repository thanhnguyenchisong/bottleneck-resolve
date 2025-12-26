## Kubernetes crash-to-expert guide (for this app)

This folder contains a staged path from zero to production-ready for the bottleneck-resolve Java app. Read in order:

1. `01-basics.md` — core K8s concepts, mental models, and CLI quickstart.
2. `02-deploy-app.md` — build/push image, deploy manifests, services, ingress, and config.
3. `03-observability.md` — metrics, logs, traces, dashboards, and alert seeds.
4. `04-scaling-resilience.md` — HPA, requests/limits, rollout strategies, disruption policies.
5. `05-performance-profiling.md` — load generation, Async Profiler recipes, targeted pod testing.
6. `06-ops-incidents.md` — day-2 ops, SLOs, incident runbook, backups, and hardening checklist.

You can apply manifests from this folder with:
```bash
kubectl apply -f k8s/app.yaml
```

Adjust namespaces, registry, and ingress according to your environment.
