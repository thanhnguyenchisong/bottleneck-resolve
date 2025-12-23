## End-to-end performance investigation guide

This guide shows how to:

1. Reproduce a performance issue.
2. Measure it with JMeter.
3. Find the bottleneck with Async Profiler.
4. Monitor in a prod-like environment with Micrometer → Prometheus → Grafana.
5. Fix the issue and verify the improvement.

### Step 1: Run the baseline app

```bash
mvn spring-boot:run
```

Hit [http://localhost:8080/work?n=20000](http://localhost:8080/work?n=20000) manually to see it is slow.

### Step 2: Measure with JMeter

- Configure the JMeter plan from [docs/03-jmeter-load-test.md](03-jmeter-load-test.md).
- Record baseline metrics: throughput, latency percentiles.

### Step 3: Profile with Async Profiler

- Start Async Profiler against the running Java process.
- Run the JMeter load.
- Inspect the flame graph and identify `WorkController.doWork` as hot.

### Step 4: Monitor with Prometheus + Grafana

- Build and start the Docker stack ([docs/02-prometheus-grafana-setup.md](02-prometheus-grafana-setup.md)).
- Send load to [http://localhost:8080/work?n=20000](http://localhost:8080/work?n=20000).
- Watch HTTP latency and JVM metrics in Grafana.

### Step 5: Optimize and verify

- Replace the inefficient list + `contains` algorithm with a better data structure.
- Rebuild, rerun JMeter, re-profile, and compare:
  - Lower CPU time in flame graph.
  - Improved latency and higher throughput in JMeter.
  - Healthier metrics in Grafana.
