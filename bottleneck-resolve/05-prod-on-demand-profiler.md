## On-demand production profiling with Async Profiler (triggered by metrics)

Use this when Grafana shows abnormal latency/CPU/throughput. The goal is to capture a short, low-overhead profile from production to locate the bottleneck the same way we do locally.

### Safety checklist

- Keep profiling windows short (15–60s) and targeted to the period of interest seen in Grafana.
- Async Profiler sampling overhead is low (usually <2% CPU), but avoid running during peak incidents unless necessary.
- Ensure you have permission to SSH/`docker exec` into the host/container and attach to the JVM.
- Prefer CPU samples first; use wall-clock sampling (`-e wall`) only if you need to see blocking/wait time.

### Steps (VM or bare-metal)

1) Locate the JVM process:

```bash
jcmd | grep performance-improve  # or jps -l
```

2) Capture a CPU profile for 30s and write to an SVG flame graph:

```bash
profiler.sh -d 30 -e cpu -f /tmp/flame-prod.svg <PID>
```

- `-d 30`: duration in seconds.
- `-e cpu`: CPU sample event (default). Use `-e wall` for wall-clock if you suspect blocking.
- Output files stay on the host; remove them after download.

3) Download the flame graph to your workstation and view:

```bash
scp user@prod-host:/tmp/flame-prod.svg ./
xdg-open flame-prod.svg  # or open in browser
```

4) Clean up on the host:

```bash
rm /tmp/flame-prod.svg
```

### Steps (Docker/K8s)

1) Identify the container/pod and PID (inside the container):

```bash
docker exec -it <container> jcmd | grep performance-improve
# or in k8s: kubectl exec -it <pod> -- jcmd | grep performance-improve
```

2) Run profiler inside the container (assuming `profiler.sh` is installed there or mounted):

```bash
docker exec <container> profiler.sh -d 30 -e cpu -f /tmp/flame-prod.svg <PID>
```

If `profiler.sh` is not inside the image, copy it in temporarily:

```bash
docker cp /path/to/async-profiler <container>:/tmp/async-profiler
```

3) Copy the flame graph out and clean up:

```bash
docker cp <container>:/tmp/flame-prod.svg ./
docker exec <container> rm /tmp/flame-prod.svg
```

### Correlate with Micrometer/Prometheus/Grafana

- Note the exact time window when latency/CPU spikes in Grafana.
- Start profiling so its window overlaps with the spike (e.g., 30s around the peak).
- In the flame graph, look for hot methods aligned with the high request rate or latency seen in Grafana.
- Repeat after a mitigation (config tweak or code change) to verify improvement.

### Tips

- To reduce noise, trigger load that matches the problematic path while profiling (if safe).
- For I/O or lock contention, try `-e wall` and optionally `-t` to include thread states.
- For memory/alloc issues, use `-e alloc` with a short duration, but note higher overhead.
- If `perf_event_open` is restricted, run with appropriate permissions/capabilities or enable perf for the user (depends on your environment).
