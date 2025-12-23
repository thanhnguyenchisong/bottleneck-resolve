## Async Profiler on Kubernetes — execution recipes

This file gives step-by-step commands for each way to run Async Profiler in K8s, so you can choose based on access and change-control constraints.

### Common pre-steps
- Pick namespace: `NS=perf`
- Pick app label: `APP_LABEL=perf-app`
- Get a pod name: `POD=$(kubectl -n "$NS" get pod -l app="$APP_LABEL" -o jsonpath='{.items[0].metadata.name}')`
- Find PID: `kubectl -n "$NS" exec -it "$POD" -- jcmd | grep performance-improve`
- Replace `<PID>` in commands below.

---

## A) Copy profiler into a running pod (no image change)
Use when you cannot change the image but can exec into pods.

1) Copy bundle (once per rollout):
```bash
kubectl -n "$NS" cp /path/to/async-profiler "$POD":/tmp/async-profiler
```
2) Capture 30s CPU profile:
```bash
kubectl -n "$NS" exec "$POD" -- /tmp/async-profiler/profiler.sh \
  -d 30 -e cpu -f /tmp/flame.svg <PID>
```
3) Retrieve and clean up:
```bash
kubectl -n "$NS" cp "$POD":/tmp/flame.svg ./flame.svg
kubectl -n "$NS" exec "$POD" -- rm /tmp/flame.svg
```

Notes: Use `-e wall` for blocking, `-e alloc` for allocation hotspots (short runs).

---

## B) Bake profiler into the application image
Use when you can update the image and want repeatability.

- Add `/opt/async-profiler` to the image in `Dockerfile`.
- Profile without copy:
```bash
kubectl -n "$NS" exec "$POD" -- /opt/async-profiler/profiler.sh \
  -d 30 -e cpu -f /tmp/flame.svg <PID>
kubectl -n "$NS" cp "$POD":/tmp/flame.svg ./flame.svg
kubectl -n "$NS" exec "$POD" -- rm /tmp/flame.svg
```

---

## C) Ephemeral container (leave main container untouched; K8s ≥1.23)
Use when exec is allowed but you prefer not to modify the main container.

1) Add ephemeral container with profiler tools:
```bash
kubectl -n "$NS" debug pod/"$POD" -c ap \
  --image=your-profiler-image --target="$APP_LABEL"
```
2) Inside the ephemeral container (automatically attaches a shell):
```bash
jcmd | grep performance-improve
/opt/async-profiler/profiler.sh -d 30 -e cpu -f /tmp/flame.svg <PID>
```
3) Copy artifact from the main pod name:
```bash
kubectl -n "$NS" cp "$POD":/tmp/flame.svg ./flame.svg
kubectl -n "$NS" exec "$POD" -- rm /tmp/flame.svg
```

---

## D) Sidecar with process namespace sharing
Use when you want profiling always available without touching the main container.

- Pod spec: set `shareProcessNamespace: true` and add a sidecar image containing Async Profiler.
- From the sidecar:
```bash
jcmd | grep performance-improve
/opt/async-profiler/profiler.sh -d 30 -e cpu -f /tmp/flame.svg <PID>
```
- Copy and clean via `kubectl cp`/`exec` as above.

---

## E) Helper/toolbox pod + kubectl cp/exec
Use when your laptop lacks tooling or you want in-cluster actions without changing the app image.

1) Start toolbox pod:
```bash
kubectl -n "$NS" run toolbox --image=ubuntu --restart=Never --command -- sleep 3600
kubectl -n "$NS" cp /path/to/async-profiler toolbox:/tmp/async-profiler
```
2) From toolbox, copy profiler into target pod and run (same as option A):
```bash
kubectl -n "$NS" cp /tmp/async-profiler "$POD":/tmp/async-profiler
kubectl -n "$NS" exec "$POD" -- /tmp/async-profiler/profiler.sh -d 30 -e cpu -f /tmp/flame.svg <PID>
kubectl -n "$NS" cp "$POD":/tmp/flame.svg ./flame.svg
```
3) Clean up toolbox after use: `kubectl -n "$NS" delete pod toolbox`

---

## F) Host-level attach (privileged/ops-only)
Use when `kubectl exec` is restricted but you have node access and perf is permitted.

1) On the node (SSH or privileged DaemonSet):
```bash
crictl ps | grep perf-app   # or docker ps
CONTAINER_ID=...
PID=$(crictl inspect "$CONTAINER_ID" | jq -r '.info.pid')  # or docker inspect
nsenter -t "$PID" -n -m -p -- /path/to/async-profiler/profiler.sh \
  -d 30 -e cpu -f /tmp/flame.svg <PID_INSIDE_CONTAINER>
```
2) Copy the artifact from the container FS as appropriate; remove it afterward.

---

## Event options and patterns
- CPU (default): `-e cpu` — lowest overhead, first choice.
- Wall-clock: `-e wall` — see blocking/locks/I/O; add `-t` for thread states.
- Allocations: `-e alloc` — for allocation hotspots; keep short.
- Duration: 15–60s typical in prod; keep windows tight.
- Output: SVG `-f /tmp/flame.svg`; text stacks `-o collapsed`.

## Correlate with signals
- Align profile window to Grafana spike times (latency/CPU/GC).
- If safe, drive targeted load (JMeter in-cluster) during the window.
- Profile one pod at a time to minimize impact.

---

## Targeting a single pod with load (to isolate one instance)
Use this when you want to reproduce and profile a bottleneck on just one pod instead of the whole deployment.

- Get the pod name and create a temporary service pointing only to it:
```bash
POD=$(kubectl -n "$NS" get pod -l app="$APP_LABEL" -o jsonpath='{.items[0].metadata.name}')
kubectl -n "$NS" expose pod "$POD" --port=8080 --target-port=8080 --name "${APP_LABEL}-direct" --type=ClusterIP
```
- Send JMeter or curl traffic to that service:
```bash
kubectl -n "$NS" port-forward svc/${APP_LABEL}-direct 18080:8080
# In another shell:
curl "http://localhost:18080/work?n=20000"
# Or set JMeter host=localhost port=18080
```
- Profile the same pod while the targeted load runs (choose any recipe above).
- Cleanup when done:
```bash
kubectl -n "$NS" delete svc/${APP_LABEL}-direct
```

Notes:
- Keep this service short-lived to avoid bypassing load balancing longer than needed.
- If Pod churns, recreate the service for the new Pod name.
