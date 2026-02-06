# Dependency Injection - Câu hỏi phỏng vấn Quarkus

## Mục lục
1. [CDI (Contexts and Dependency Injection)](#cdi)
2. [Bean Scopes](#bean-scopes)
3. [Qualifiers](#qualifiers)
4. [Programmatic Lookup: Instance&lt;T&gt; và Provider&lt;T&gt;](#programmatic-lookup)
5. [Producers và Disposers](#producers-và-disposers)
6. [Lifecycle callbacks](#lifecycle-callbacks)
7. [Events](#events)
8. [Interceptors](#interceptors)
9. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

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

### Dependent (mặc định)

```java
// @Dependent: Một instance mới cho mỗi injection point (default nếu không khai báo scope)
// Pseudo-scope: không có context riêng, sống theo bean chứa nó
@Dependent
public class HelperService {
    // Mỗi bean inject HelperService sẽ có 1 instance HelperService riêng
}

@ApplicationScoped
public class UserService {
    @Inject
    HelperService helper;  // Instance A
}

@ApplicationScoped
public class OrderService {
    @Inject
    HelperService helper;  // Instance B (khác Instance A)
}
```

---

## Qualifiers

### @Named

```java
// @Named: Qualifier có sẵn, dùng name (string)
@Named("postgres")
@ApplicationScoped
public class PostgresRepo implements UserRepository { }

@Inject
@Named("postgres")
UserRepository repo;
```

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

## Programmatic Lookup

### Instance&lt;T&gt;

```java
// Instance<T>: Lookup programmatic, lazy, hoặc lấy nhiều bean cùng type
@ApplicationScoped
public class PaymentRouter {
    @Inject
    Instance<PaymentHandler> handlers;  // Tất cả bean implement PaymentHandler

    public void pay(Order order) {
        for (PaymentHandler h : handlers) {
            if (h.supports(order.getMethod())) {
                h.handle(order);
                return;
            }
        }
    }
}

// Optional / lazy: chỉ resolve khi cần
@Inject
Instance<OptionalFeature> optionalFeature;

void use() {
    if (optionalFeature.isUnsatisfied()) return;  // Không có bean nào
    if (optionalFeature.isAmbiguous()) return;   // Nhiều bean, cần qualifier
    optionalFeature.get().doSomething();         // Lazy get
}
```

### Provider&lt;T&gt;

```java
// javax.inject.Provider<T>: Lazy — mỗi lần get() mới resolve (theo scope)
@ApplicationScoped
public class ReportService {
    @Inject
    Provider<RequestScopedReportContext> contextProvider;

    public Report build() {
        RequestScopedReportContext ctx = contextProvider.get();  // Lấy instance theo request
        return ctx.buildReport();
    }
}
```

| | Instance&lt;T&gt; | Provider&lt;T&gt; |
|---|------------------|-------------------|
| **Mục đích** | Nhiều bean, optional, iterate | Lazy đơn giản |
| **API** | `get()`, `isUnsatisfied()`, `isAmbiguous()`, `iterator()` | Chỉ `get()` |

---

## Producers và Disposers

### @Produces

```java
// Tạo bean theo điều kiện (config, factory)
@ApplicationScoped
public class DataSourceProducer {
    @Produces
    @ApplicationScoped
    public DataSource createDataSource(
            @ConfigProperty(name = "db.url") String url,
            @ConfigProperty(name = "db.user") String user) {
        return DataSources.create(url, user);
    }

    void dispose(@Disposes DataSource ds) {
        // Cleanup khi context kết thúc
        ds.close();
    }
}
```

---

## Lifecycle callbacks

```java
@ApplicationScoped
public class StartupService {
    @PostConstruct
    void init() {
        // Chạy sau khi bean được tạo và inject xong
    }

    @PreDestroy
    void shutdown() {
        // Chạy trước khi context bị destroy
    }
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

### Q2: Instance&lt;T&gt; dùng khi nào?

- Cần **nhiều bean** cùng type (iterate, chọn theo runtime).
- Dependency **optional** (kiểm tra `isUnsatisfied()`).
- **Lazy** resolve (chỉ get khi cần).

### Q3: @Dependent khác gì ApplicationScoped?

- **@Dependent**: mỗi injection point một instance riêng, lifecycle gắn với bean chứa nó (pseudo-scope).
- **@ApplicationScoped**: một instance dùng chung toàn app (normal scope).

---

## Best Practices

1. **Use CDI**: Standard, powerful
2. **Constructor injection**: Recommended
3. **Proper scopes**: Choose right scope (kể cả @Dependent khi cần)
4. **Use qualifiers**: For multiple implementations
5. **Instance/Provider**: Optional hoặc programmatic lookup khi cần
6. **@Produces**: Khi tạo bean từ config/factory
7. **Events**: For decoupled communication
8. **@PostConstruct/@PreDestroy**: Khởi tạo và dọn dẹp đúng lifecycle

---

## Tổng kết

- **CDI**: Standard dependency injection
- **Bean Scopes**: ApplicationScoped, RequestScoped, Singleton, **Dependent**
- **Qualifiers**: Multiple implementations
- **Instance&lt;T&gt; / Provider&lt;T&gt;**: Programmatic, lazy, optional
- **Producers/Disposers**: Tạo và hủy bean theo điều kiện
- **Lifecycle**: @PostConstruct, @PreDestroy
- **Events**: Decoupled communication
- **Interceptors**: Cross-cutting concerns
