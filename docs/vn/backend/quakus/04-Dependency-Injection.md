# Dependency Injection - Câu hỏi phỏng vấn Quarkus

## Mục lục
1. [CDI (Contexts and Dependency Injection)](#cdi)
2. [Bean Scopes](#bean-scopes)
3. [Qualifiers](#qualifiers)
4. [Events](#events)
5. [Interceptors](#interceptors)
6. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## CDI (Contexts and Dependency Injection)

### CDI Basics

```java
// CDI: Jakarta Contexts and Dependency Injection
// Standard dependency injection

// Service
@ApplicationScoped
public class UserService {
    public User findById(Long id) {
        return userRepository.findById(id);
    }
}

// Resource
@Path("/users")
public class UserResource {
    @Inject
    UserService userService;  // Injected by CDI
    
    @GET
    @Path("/{id}")
    public User getUser(@PathParam("id") Long id) {
        return userService.findById(id);
    }
}
```

### Constructor Injection

```java
// Constructor injection (recommended)
@ApplicationScoped
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

---

## Bean Scopes

### Application Scoped

```java
// ApplicationScoped: One instance per application
@ApplicationScoped
public class UserService {
    // Shared across all requests
    // Thread-safe required
}
```

### Request Scoped

```java
// RequestScoped: One instance per HTTP request
@RequestScoped
public class RequestContext {
    // New instance per request
    // Request-specific data
}
```

### Singleton

```java
// Singleton: One instance (like ApplicationScoped)
@Singleton
public class ConfigService {
    // One instance
}
```

---

## Qualifiers

### Custom Qualifiers

```java
// Qualifier annotation
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface Database {
    DatabaseType value();
    
    enum DatabaseType {
        POSTGRES, MYSQL
    }
}

// Implementation
@Database(DatabaseType.POSTGRES)
@ApplicationScoped
public class PostgresUserRepository implements UserRepository {
    // PostgreSQL implementation
}

@Database(DatabaseType.MYSQL)
@ApplicationScoped
public class MySQLUserRepository implements UserRepository {
    // MySQL implementation
}

// Usage
@ApplicationScoped
public class UserService {
    @Inject
    @Database(DatabaseType.POSTGRES)
    UserRepository userRepository;
}
```

---

## Events

### CDI Events

```java
// Event: Decoupled communication
// Publisher
@ApplicationScoped
public class OrderService {
    @Inject
    Event<OrderCreatedEvent> orderCreatedEvent;
    
    public void createOrder(Order order) {
        orderRepository.save(order);
        orderCreatedEvent.fire(new OrderCreatedEvent(order.getId()));
    }
}

// Observer
@ApplicationScoped
public class NotificationService {
    void onOrderCreated(@Observes OrderCreatedEvent event) {
        sendNotification(event.getOrderId());
    }
}
```

---

## Interceptors

### Custom Interceptor

```java
// Interceptor annotation
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Logged {
}

// Interceptor implementation
@Logged
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggingInterceptor {
    @AroundInvoke
    public Object log(InvocationContext context) throws Exception {
        System.out.println("Before: " + context.getMethod().getName());
        Object result = context.proceed();
        System.out.println("After: " + context.getMethod().getName());
        return result;
    }
}

// Usage
@Logged
@ApplicationScoped
public class UserService {
    public User findById(Long id) {
        // Interceptor logs before/after
        return userRepository.findById(id);
    }
}
```

---

## Câu hỏi thường gặp

### Q1: CDI vs Spring DI?

```java
// CDI:
// - Jakarta EE standard
// - More powerful (events, interceptors)
// - Build-time optimization

// Spring DI:
// - Spring-specific
// - Simpler
// - Runtime processing
```

---

## Best Practices

1. **Use CDI**: Standard, powerful
2. **Constructor injection**: Recommended
3. **Proper scopes**: Choose right scope
4. **Use qualifiers**: For multiple implementations
5. **Events**: For decoupled communication

---

## Tổng kết

- **CDI**: Standard dependency injection
- **Bean Scopes**: ApplicationScoped, RequestScoped, Singleton
- **Qualifiers**: Multiple implementations
- **Events**: Decoupled communication
- **Interceptors**: Cross-cutting concerns
