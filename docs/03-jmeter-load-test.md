## JMeter load test for `/work`

### 1. Test plan idea

Create a simple JMeter test plan:

- **Thread Group**:
  - Threads (users): 50–200
  - Ramp-up period: 10–30 seconds
  - Loop count: forever (or a large number)
- **HTTP Request**:
  - Method: GET
  - Path: `/work`
  - Parameters: `n=20000`
  - Server name or IP: `localhost`
  - Port: `8080`
- **Listeners**:
  - Summary Report
  - Aggregate Report

### 2. Running the test

1. Start the Spring Boot app locally.
2. Open JMeter GUI, configure the plan as above (or use a `.jmx` file if you create one).
3. Start the test and watch:
   - Response times (P90, P95, P99).
   - Throughput.
   - Error percentage.

### 3. Combining with profiling and metrics

- While JMeter is running, capture a CPU profile with Async Profiler.
- If running via Docker stack, observe Prometheus / Grafana dashboards at the same time.
