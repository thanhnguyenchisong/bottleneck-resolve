# Native Image - Câu hỏi phỏng vấn Quarkus

## Mục lục
1. [GraalVM Native Image](#graalvm-native-image)
2. [Build Native Executable](#build-native-executable)
3. [Native Image Limitations](#native-image-limitations)
4. [Performance](#performance)
5. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## GraalVM Native Image

### What is Native Image?

**GraalVM Native Image** compiles Java applications ahead-of-time to native executables.

### Benefits

```java
// Benefits:
// 1. Fast startup: Milliseconds (vs seconds)
// 2. Low memory: Minimal RAM usage
// 3. Single executable: No JVM needed
// 4. Container-friendly: Small images
```

---

## Build Native Executable

### Maven

```bash
# Build native image
./mvnw package -Pnative

# Requires: GraalVM installed
# Result: executable file (no JVM)
```

### Docker

```bash
# Build native image trong Docker
./mvnw package -Pnative -Dquarkus.native.container-build=true

# Uses Docker để build (no local GraalVM needed)
```

---

## Native Image Limitations

### Reflection

```java
// Reflection: Must be configured
// Register classes for reflection

@RegisterForReflection
public class User {
    // Registered for reflection
}

// Or in application.properties
quarkus.native.additional-build-args=--initialize-at-build-time=com.example.User
```

### Dynamic Features

```java
// Limitations:
// - No dynamic class loading
// - Limited reflection
// - No JNI
// - Some libraries not supported
```

---

## Performance

### Startup Time

```java
// JVM: 2-3 seconds
// Native: 50-100ms
// Improvement: 20-60x faster
```

### Memory Usage

```java
// JVM: 150-300 MB
// Native: 50-100 MB
// Improvement: 2-3x less memory
```

---

## Câu hỏi thường gặp

### Q1: Khi nào dùng Native Image?

```java
// Use Native Image khi:
// - Serverless (AWS Lambda, etc.)
// - Containers (small images)
// - Fast startup required
// - Low memory required

// Don't use khi:
// - Complex reflection
// - Dynamic features
// - Long-running applications (JIT better)
```

---

## Best Practices

1. **Test native**: Test native builds
2. **Configure reflection**: Register classes
3. **Profile guided**: Use PGO for optimization
4. **Monitor**: Track native performance

---

## Tổng kết

- **Native Image**: Ahead-of-time compilation
- **Benefits**: Fast startup, low memory
- **Limitations**: Reflection, dynamic features
- **Performance**: 20-60x faster startup
- **Best Practices**: Test, configure, monitor
