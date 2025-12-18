## Local profiling with Async Profiler

### 1. Build and run the app

```bash
mvn clean package -DskipTests
java -jar target/performance-improve-0.0.1-SNAPSHOT.jar
```

Hit the slow endpoint in another terminal:

```bash
curl "http://localhost:8080/work?n=20000"
```

### 2. Start Async Profiler

Assuming `async-profiler` is installed and `profiler.sh` is on your PATH:

```bash
# Find the PID of the Java process
jps -l

# Example: PID 12345
profiler.sh -d 60 -f /tmp/flame-work.svg 12345

# In version 4.2 and later, you can use:
./asprof -d 60 -f flamegraph.html 4236

# If command is not found then use this command before running asprof
chmod +x asprof

# some the other options you can use with asprof
# get CPU profile
./asprof -e cpu -d 60 -f result.html 4236
# get information for specific java method
./asprof -e 'com.example.perf.controller.WorkController.doWork' -d 60 -f result.html 4236
# get heap allocation profile
./asprof -e alloc -d 60 -f result.html 4236
```

- **`-d 60`**: profile for 60 seconds while you send load (e.g., with JMeter).
- **`-f`**: output file in SVG format.

Open the SVG in a browser and look for hot methods around `WorkController.doWork` and `List.contains`.

### 3. Optimize and compare

Later, you can replace the list + `contains` pattern with a more efficient structure (e.g., `HashSet`) and re-run the same profiling session to compare flame graphs.
