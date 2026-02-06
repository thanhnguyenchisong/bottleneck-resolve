# Reactive Programming - Câu hỏi phỏng vấn Quarkus

## Mục lục
1. [Mutiny](#mutiny)
2. [Uni và Multi Operations](#uni-và-multi-operations)
3. [Infrastructure & Context Propagation](#infrastructure-&-context-propagation)
4. [Reactive Streams](#reactive-streams)
5. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## Mutiny

### What is Mutiny?

**Mutiny** là reactive programming library của Quarkus, designed for event-driven và non-blocking programming.

### Uni

```java
// Uni: Single value (like Mono in Reactor, Single in RxJava)
Uni<String> name = Uni.createFrom().item("John");

// Transform
Uni<String> upper = name.map(String::toUpperCase);

// Chain operations
Uni<String> result = getUserAsync(id)
    .map(User::getName)
    .map(String::toUpperCase)
    .onFailure().recoverWithItem("Unknown");
```

### Multi

```java
// Multi: Multiple values (like Flux in Reactor, Observable in RxJava)
Multi<String> names = Multi.createFrom().items("John", "Jane", "Bob");

// Transform
Multi<String> upper = names.map(String::toUpperCase);

// Filter
Multi<String> filtered = names.filter(name -> name.startsWith("J"));
```

---

## Uni và Multi

### Uni Operations

```java
// Create Uni
Uni<String> uni = Uni.createFrom().item("value");
Uni<String> uni = Uni.createFrom().failure(new Exception("Error"));

// Transform
Uni<String> transformed = uni.map(String::toUpperCase);

// FlatMap
Uni<String> result = getUserAsync(id)
    .flatMap(user -> getProfileAsync(user.getId()));

// Error handling
Uni<String> result = uni
    .onFailure().recoverWithItem("default")
    .onFailure().retry().atMost(3);
```

### Multi Operations

```java
// Create Multi
Multi<String> multi = Multi.createFrom().items("a", "b", "c");
Multi<String> multi = Multi.createFrom().range(1, 10).map(String::valueOf);

// Transform
Multi<String> transformed = multi.map(String::toUpperCase);

// Filter
Multi<String> filtered = multi.filter(s -> s.length() > 1);

// Collect
Uni<List<String>> list = multi.collect().asList();
```

### Advanced Operations

```java
// Combine (Parallel Execution)
Uni<String> u1 = service.call1();
Uni<Integer> u2 = service.call2();

// Chạy song song u1 và u2, đợi cả 2 xong
Uni<Tuple2<String, Integer>> both = Uni.combine().all().unis(u1, u2).asTuple();

// Bridging: Blocking -> Reactive
Uni<String> blockingBridge = Uni.createFrom().emitter(emitter -> {
    try {
        String result = heavyBlockingMethod();
        emitter.complete(result);
    } catch (Exception e) {
        emitter.fail(e);
    }
});
```

---

## Infrastructure & Context Propagation

### Event Loop vs Worker Pool

- **Event Loop (IO Thread)**: Xử lý non-blocking code. Ít thread, không được block.
- **Worker Pool**: Xử lý blocking code (JDBC, File IO).

```java
// Chuyển đổi thread context

// 1. emitOn: Chỉ định thread pool thực thi upstream (nguồn)
Uni.createFrom().item(data)
   .emitOn(Infrastructure.getDefaultWorkerPool()) // Chạy trên worker thread
   .map(this::heavyProcess);

// 2. runSubscriptionOn: Chỉ định thread khi subscribe
```

### Context Propagation
Quarkus tự động propagate context (CDI, Transaction, Security) trong Reactive pipeline, nhưng cần lưu ý khi manually switch thread.

```java
// SmallRye Context Propagation tự động xử lý
// SecurityContext sẽ có sẵn ở downstream
authService.login()
    .flatMap(token -> service.doSecureAction()); 
```

---

## Reactive Streams

### Backpressure

```java
// Backpressure: Consumer controls flow
Multi<String> items = Multi.createFrom().range(1, 1000)
    .map(String::valueOf);

// Consumer requests items as needed
items.subscribe().with(
    item -> process(item),  // onItem
    failure -> handleError(failure),  // onFailure
    () -> complete()  // onCompletion
);
```

---

## Câu hỏi thường gặp

### Q1: Mutiny vs Reactor?

```java
// Mutiny:
// - Quarkus native
// - Simpler API
// - Better integration

// Reactor:
// - Spring ecosystem
// - More features
// - Larger community
```

---

## Best Practices

1. **Use reactive**: For I/O-bound operations
2. **Handle errors**: onFailure handlers
3. **Backpressure**: Control flow
4. **Non-blocking**: Use Uni/Multi

---

## Tổng kết

- **Mutiny**: Reactive programming library
- **Uni**: Single value
- **Multi**: Multiple values
- **Reactive Streams**: Backpressure support
- **Non-blocking I/O**: Better performance
