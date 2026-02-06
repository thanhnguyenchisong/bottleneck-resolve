# Dependency Injection - Câu hỏi phỏng vấn Quarkus

## Mục lục
1. [CDI (Contexts and Dependency Injection)](#cdi)
2. [Bean Scopes](#bean-scopes)
3. [Qualifiers](#qualifiers)
4. [Programmatic Lookup: Instance&lt;T&gt; và Provider&lt;T&gt;](#programmatic-lookup)
5. [Producers và Disposers](#producers-và-disposers)
6. [Lifecycle callbacks](#lifecycle-callbacks)
7. [Advanced CDI Features](#advanced-cdi-features)
8. [Events](#events)
9. [Interceptors](#interceptors)
10. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## CDI (Contexts and Dependency Injection)

### CDI Basics & ArC (Quarkus)

Quarkus sử dụng **ArC** - một implement CDI build-time optimization.
- **Build-time**: Phân tích metadata lúc build, không dùng reflection lúc runtime (trừ khi cần thiết), giúp startup cực nhanh.
- **Unused beans**: Mặc định Quarkus xóa các bean không được dùng (`quarkus.arc.remove-unused-beans=true`) để giảm memory.

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
```

### Normal Scope vs Pseudo Scope (Client Proxy)

#### Bảng so sánh

| Đặc điểm | Normal Scope | Pseudo Scope |
| :--- | :--- | :--- |
| **Annotations** | `@ApplicationScoped`, `@RequestScoped`, `@SessionScoped` | `@Dependent` (default), `@Singleton` |
| **Cơ chế Inject** | **Client Proxy** (Vỏ bọc) | **Direct Reference** (Instance thật) |
| **Lifecycle** | Có điểm bắt đầu/kết thúc rõ ràng do Context quản lý. | Phụ thuộc vào bean chứa nó (`@Dependent`) hoặc sống mãi (`@Singleton`). |
| **Lazy Init** | **Có**. Bean thật chỉ được tạo khi gọi hàm đầu tiên. | **Không**. Tạo ngay khi được inject. |

#### Client Proxy là gì?

**Client Proxy** là một object giả mà CDI inject vào thay vì object thật.
- **Giải quyết Scope Mismatch**: Khi inject bean scope ngắn (Request) vào bean scope dài (Application), Proxy đảm bảo gọi đúng instance của request hiện tại.
- **Lazy Initialization**: Tăng tốc startup vì chưa cần tạo instance thật ngay.
- **Circular Dependency**: Giúp phá vỡ vòng lặp dependency trong một số trường hợp.

#### Khi nào dùng cái nào?

1. **@ApplicationScoped** (Normal):
   - **Dùng cho**: Service, Repository, Component stateless hoặc shared state.
   - **Lợi ích**: Tiết kiệm RAM (1 instance), Lazy Init, Thread-safe.

2. **@RequestScoped** (Normal):
   - **Dùng cho**: User context, Transaction info, Request logging.
   - **Lợi ích**: Cô lập dữ liệu giữa các request.

3. **@Dependent** (Pseudo - Default):
   - **Dùng cho**: Helper object, object dùng 1 lần, hoặc cần nhiều instance riêng biệt cho từng cha.
   - **Cảnh báo**: Nếu inject vào `@ApplicationScoped`, bean Dependent sẽ **sống mãi** (Memory Leak tiềm ẩn).

4. **@Singleton** (Pseudo):
   - **Dùng cho**: Utility đơn giản không cần Proxy.
   - **Lưu ý**: Không Lazy Init, startup chậm hơn nếu init nặng. Ưu tiên `@ApplicationScoped` trong Quarkus.

#### Vấn đề thường gặp (Pitfalls)

- **Gọi field trực tiếp**: `proxy.field` có thể null hoặc sai giá trị (vì không qua method delegate). -> **Luôn dùng Getter/Setter**.
- **Private Methods**: Proxy chuẩn không gọi được private method (Quarkus fix được bằng bytecode transformation nhưng nên hạn chế).

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

## Advanced CDI Features

### Alternatives & Priority

Thay thế implementation bean (ví dụ cho môi trường test hoặc mock).

```java
// Default implementation
@ApplicationScoped
public class RealPaymentService implements PaymentService { ... }

// Mock implementation (active khi có priority cao hơn)
@Alternative
@Priority(1) // Priority cao hơn default
@ApplicationScoped
public class MockPaymentService implements PaymentService { ... }
```

### Stereotypes

Gom nhóm các annotation lại thành một annotation mới để tái sử dụng.

```java
@Stereotype
@ApplicationScoped
@Transactional
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {
    // Giờ chỉ cần dùng @Service thay vì viết lại cả 3 dòng
}
```

### Decorators

Bọc (wrap) một bean để thêm logic business (khác Interceptor là kỹ thuật, Decorator là business).

```java
@Decorator
@Priority(10)
public class DiscountDecorator implements PriceCalculator {
    @Inject
    @Delegate // Inject instance gốc
    @Any
    PriceCalculator delegate;

    public double calculate(Product p) {
        double original = delegate.calculate(p);
        return original * 0.9; // Giảm giá 10%
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
        // Sync event
        orderCreatedEvent.fire(new OrderCreatedEvent(order.getId()));
        
        // Async event (trả về CompletionStage)
        orderCreatedEvent.fireAsync(new OrderCreatedEvent(order.getId()))
            .thenAccept(e -> System.out.println("Async finished"));
    }
}

// Observer
@ApplicationScoped
public class NotificationService {
    // Synchronous (chạy cùng thread publisher)
    void onOrderCreated(@Observes OrderCreatedEvent event) {
        sendNotification(event.getOrderId());
    }

    // Asynchronous (chạy thread khác)
    void onOrderCreatedAsync(@ObservesAsync OrderCreatedEvent event) {
        // Heavy processing
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
- **@ApplicationScoped**: một instance dùng chung toàn app (normal scope), inject qua **Client Proxy**.

### Q4: Client Proxy là gì?

- Là object trung gian mà CDI inject vào thay vì instance thật (với Normal Scope).
- Giúp lazy initialization và xử lý scope mismatch (VD: inject RequestScoped vào ApplicationScoped).
- Khi gọi method trên proxy, nó mới tìm instance thật trong context hiện tại để delegate.

---

## Best Practices

1. **Use CDI**: Standard, powerful
2. **Constructor injection**: Recommended
3. **Proper scopes**: Hiểu rõ Client Proxy vs Direct Reference.
4. **Package-private**: Trong Quarkus, nên để field/method injection là package-private (bỏ `private`) để tối ưu reflection.
5. **Use qualifiers**: For multiple implementations
6. **Instance/Provider**: Optional hoặc programmatic lookup khi cần
7. **Events**: Decoupled communication (dùng Async nếu task nặng)

---

## Tổng kết

- **CDI/ArC**: Build-time dependency injection.
- **Bean Scopes**: Normal (@ApplicationScoped) vs Pseudo (@Dependent).
- **Client Proxy**: Cơ chế inject của Normal scopes.
- **Advanced**: Alternatives, Stereotypes, Decorators.
- **Events**: Sync vs Async observers.
