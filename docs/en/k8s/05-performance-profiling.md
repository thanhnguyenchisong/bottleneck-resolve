## Part 5: Performance testing and profiling on K8s

Goal: load-test the app in-cluster and pinpoint code bottlenecks with Async Profiler.

### 1) In-cluster JMeter (headless)
- Keep traffic inside the cluster for realistic latency.
```bash
kubectl -n perf run jmeter --image=justb4/jmeter:5.6.3 --restart=Never --command -- sleep 3600
kubectl -n perf cp docs/03-jmeter-load-test.md jmeter:/tmp/README.md   # optional reference
kubectl -n perf cp work-load.jmx jmeter:/tmp/work-load.jmx
kubectl -n perf exec -it jmeter -- jmeter -n -t /tmp/work-load.jmx \
  -JserverHost=perf-app.perf.svc.cluster.local \
  -JserverPort=80 \
  -Jn=20000 \
  -l /tmp/result.jtl -e -o /tmp/report
kubectl -n perf cp jmeter:/tmp/report ./report
```

### 2) Target a single pod (isolate an instance)
- Expose one pod temporarily:
```bash
POD=$(kubectl -n perf get pod -l app=perf-app -o jsonpath='{.items[0].metadata.name}')
kubectl -n perf expose pod "$POD" --port=8080 --target-port=8080 --name perf-app-direct --type=ClusterIP
kubectl -n perf port-forward svc/perf-app-direct 18080:8080
curl "http://localhost:18080/work?n=20000"
```
- Run JMeter against `localhost:18080` to stress only that pod, then profile it.
- Clean up: `kubectl -n perf delete svc/perf-app-direct`

### 3) Async Profiler recipes (choose one)
See `docs/07-async-profiler-k8s-recipes.md` for full commands. Summary:
- Copy bundle into pod:
```bash
kubectl -n perf cp /path/to/async-profiler "$POD":/tmp/async-profiler
kubectl -n perf exec "$POD" -- /tmp/async-profiler/profiler.sh -d 30 -e cpu -f /tmp/flame.svg <PID>
kubectl -n perf cp "$POD":/tmp/flame.svg ./flame.svg
```
- Ephemeral container (no image change): `kubectl -n perf debug pod/"$POD" -c ap --image=your-profiler-image --target=perf-app`
- Baked-in image: run profiler from `/opt/async-profiler/...`
- Sidecar with `shareProcessNamespace: true` for repeatable profiling.
- Host-level attach if `kubectl exec` is restricted.

Event choices: `-e cpu` (default), `-e wall` (blocking/locks/I/O), `-e alloc` (alloc hotspots). Keep runs 15–60s.

### 4) Correlate with metrics
- Note spike times in Grafana (HTTP latency, CPU, GC).
- Run profiler overlapping the spike window and while JMeter load is active.
- Verify improvement after code/config changes by repeating load + profile.

### 5) Quick perf triage checklist
- CPU high + flame graph hot method → optimize code path.
- GC high, heap near cap → increase heap or fix alloc churn.
- Throttling (metrics-server shows CPU limit hit) → adjust limits or requests.
- One pod hotter → check node placement/affinity and targeted profile that pod.

Next: Part 6 covers day-2 ops, incidents, and hardening.
