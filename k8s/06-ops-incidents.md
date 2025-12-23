## Part 6: Day-2 ops, incidents, and hardening

Goal: keep the service reliable in production, handle incidents, and reduce risk.

### 1) SLOs and budgets
- Example SLO: 99.5% success over 30d; P95 latency < 500ms; error budget = 0.5% failures.
- Track via Grafana/Prometheus; alert on budget burn rate, not just point metrics.

### 2) Runbook for incidents
1) **Detect**: Alerts fire (5xx rate, latency P95, high CPU/GC, restarts).
2) **Triage fast**:
   - `kubectl -n perf get pods -o wide`
   - `kubectl -n perf describe pod <pod>` for events
   - `kubectl -n perf logs <pod> | tail`
   - `kubectl -n perf top pods`
3) **Scope**: Is it one pod, node-wide, or all zones?
4) **Mitigate**:
   - Scale up: `kubectl -n perf scale deploy/perf-app --replicas=6`
   - Roll back: `kubectl -n perf rollout undo deploy/perf-app`
   - Restart bad pod: `kubectl -n perf delete pod <pod>`
5) **Diagnose deeper**:
   - Check Grafana panels (HTTP, JVM, GC, node resources).
   - Profile hot pod with Async Profiler (see Part 5/`docs/07-async-profiler-k8s-recipes.md`).
6) **Close**: Verify metrics recovered, create postmortem, track action items.

### 3) Backups and config safety
- Store manifests in git; use tags for deployable versions.
- Back up Grafana dashboards and alert rules (via config or API).
- Back up any stateful dependencies (DB/storage) separately; this app is stateless but depends on them.

### 4) Security/hardening checklist
- Do not run as root: set `securityContext.runAsNonRoot: true` and a non-root UID.
- Read-only root FS where possible; drop unnecessary capabilities.
- Use NetworkPolicies to restrict ingress/egress if cluster supports it.
- Keep images small and updated; scan images regularly.
- Rotate secrets; avoid mounting secrets as files if env vars suffice.

### 5) Reliability settings to consider
- PodDisruptionBudget (Part 4) to protect capacity during drains.
- Liveness/readiness tuned to real startup/steady-state behavior.
- HPA configured to avoid runaway scale-down (set sensible minReplicas).
- Topology spread constraints to avoid single-node concentration.

### 6) Change management
- Prefer immutable image tags per release.
- Roll out with surge 1 / unavailable 0 for low-risk changes; canary for risky ones.
- After rollout, watch key metrics for 10–15 minutes before calling it good.

### 7) Useful ops commands
```bash
kubectl -n perf get pods -o wide
kubectl -n perf describe deploy/perf-app
kubectl -n perf rollout status deploy/perf-app
kubectl -n perf rollout undo deploy/perf-app
kubectl -n perf top pods
kubectl -n perf logs -l app=perf-app --max-log-requests=1 --tail=200
```

Keep this runbook close to the dashboards and the profiler recipes so responders can act quickly.
