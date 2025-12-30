# Reactive Programming - Câu hỏi phỏng vấn Quarkus

## Mục lục
1. [Mutiny](#mutiny)
2. [Uni và Multi](#uni-và-multi)
3. [Reactive Streams](#reactive-streams)
4. [Non-blocking I/O](#non-blocking-io)
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

## Non-blocking I/O

### Reactive REST

```java
// Reactive REST endpoint
@Path("/users")
public class UserResource {
    @Inject
    UserService userService;
    
    @GET
    @Path("/{id}")
    public Uni<User> getUser(@PathParam("id") Long id) {
        return userService.findByIdAsync(id);
    }
    
    @GET
    public Multi<User> getAllUsers() {
        return userService.findAllAsync();
    }
}
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
