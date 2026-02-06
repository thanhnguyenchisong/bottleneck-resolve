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

### Tại sao cần Programmatic Lookup?

Bình thường `@Inject` resolve bean lúc **build-time** (cố định). Nhưng có những lúc bạn cần:
- Chọn bean **lúc runtime** (theo config, input user).
- Dependency **optional** (có thể không tồn tại).
- **Lazy**: Chỉ tạo bean khi thật sự cần.
- Lấy **nhiều bean** cùng interface (Strategy/Plugin pattern).

-> Dùng `Instance<T>` hoặc `Provider<T>`.

---

### Instance&lt;T&gt;

`jakarta.enterprise.inject.Instance<T>` — CDI API, mạnh mẽ nhất.

#### Khi nào NÊN dùng Instance&lt;T&gt;

**1. Strategy / Plugin Pattern — Nhiều bean cùng interface, chọn lúc runtime**

```java
// Có nhiều PaymentHandler: Momo, VNPay, Stripe...
// Không biết trước dùng cái nào -> chọn lúc runtime
@ApplicationScoped
public class PaymentRouter {
    @Inject
    Instance<PaymentHandler> handlers;  // Inject TẤT CẢ implementations

    public void pay(Order order) {
        for (PaymentHandler h : handlers) {
            if (h.supports(order.getPaymentMethod())) {
                h.handle(order);
                return;
            }
        }
        throw new UnsupportedPaymentException(order.getPaymentMethod());
    }
}
```

**2. Optional Dependency — Bean có thể không tồn tại**

```java
// Feature toggle: module analytics có thể không được cài
@ApplicationScoped
public class DashboardService {
    @Inject
    Instance<AnalyticsService> analytics;

    public Dashboard load() {
        Dashboard d = buildBasicDashboard();
        if (analytics.isResolvable()) {          // Có bean không?
            d.setStats(analytics.get().getStats());
        }
        // Nếu không có AnalyticsService -> App vẫn chạy bình thường
        return d;
    }
}
```

**3. Chọn bean bằng Qualifier lúc runtime**

```java
@Inject
@Any  // Inject tất cả, kể cả bean có qualifier khác nhau
Instance<NotificationSender> senders;

public void send(String channel, String msg) {
    // Select theo qualifier lúc runtime
    Instance<NotificationSender> selected = senders
        .select(new ChannelLiteral(channel));  // VD: "email", "sms", "push"
    
    if (selected.isResolvable()) {
        selected.get().send(msg);
    }
}
```

**4. Destroy @Dependent bean thủ công (tránh Memory Leak)**

```java
@Inject
Instance<HeavyWorker> workerInstance;  // HeavyWorker là @Dependent

public void process() {
    Instance.Handle<HeavyWorker> handle = workerInstance.getHandle();
    HeavyWorker worker = handle.get();
    try {
        worker.doWork();
    } finally {
        handle.destroy();  // Giải phóng ngay, không chờ bean cha chết
    }
}
```

#### API chính của Instance&lt;T&gt;

| Method | Mô tả |
| :--- | :--- |
| `get()` | Lấy 1 instance (ném `AmbiguousResolutionException` nếu > 1 bean) |
| `isResolvable()` | `true` nếu có đúng 1 bean match |
| `isUnsatisfied()` | `true` nếu không có bean nào |
| `isAmbiguous()` | `true` nếu có nhiều bean (cần qualifier để chọn) |
| `iterator()` / `stream()` | Duyệt tất cả beans |
| `select(Qualifier...)` | Lọc bean theo qualifier lúc runtime |
| `getHandle()` | Trả về Handle để quản lý lifecycle (`destroy()`) |

---

### Provider&lt;T&gt;

`jakarta.inject.Provider<T>` — JSR-330, đơn giản hơn, chỉ có `get()`.

#### Khi nào NÊN dùng Provider&lt;T&gt;

**1. Lazy Init đơn giản — Trì hoãn tạo bean nặng**

```java
@ApplicationScoped
public class ReportService {
    @Inject
    Provider<PdfEngine> pdfEngineProvider;  // PdfEngine init rất nặng (load font, template...)

    public void export(Report report) {
        if (report.needsPdf()) {
            PdfEngine engine = pdfEngineProvider.get();  // Chỉ init khi thật sự cần xuất PDF
            engine.render(report);
        }
        // Nếu không cần PDF -> PdfEngine KHÔNG BAO GIỜ được tạo -> tiết kiệm tài nguyên
    }
}
```

**2. Lấy đúng instance theo Scope hiện tại — Request-scoped bean trong Application-scoped bean**

```java
@ApplicationScoped
public class AuditService {
    @Inject
    Provider<SecurityContext> securityCtxProvider;  // @RequestScoped

    public void log(String action) {
        // Mỗi lần gọi get() -> lấy SecurityContext của REQUEST HIỆN TẠI
        String user = securityCtxProvider.get().getCurrentUser();
        auditRepo.save(new AuditLog(user, action));
    }
}
```

**3. Tạo nhiều instance @Dependent mới**

```java
@ApplicationScoped
public class TaskManager {
    @Inject
    Provider<TaskWorker> workerProvider;  // TaskWorker là @Dependent

    public void runBatch(List<Task> tasks) {
        for (Task t : tasks) {
            TaskWorker worker = workerProvider.get();  // Mỗi lần get() -> instance MỚI
            worker.execute(t);
        }
    }
}
```

---

### So sánh Instance&lt;T&gt; vs Provider&lt;T&gt;

| | Instance&lt;T&gt; | Provider&lt;T&gt; |
| :--- | :--- | :--- |
| **Spec** | CDI (Jakarta EE) | JSR-330 (javax/jakarta.inject) |
| **API** | `get()`, `isUnsatisfied()`, `isAmbiguous()`, `select()`, `stream()`, `getHandle()` | Chỉ `get()` |
| **Nhiều bean** | Iterate / select lúc runtime | Không hỗ trợ |
| **Optional check** | `isResolvable()`, `isUnsatisfied()` | Không (ném exception nếu không có) |
| **Destroy thủ công** | `getHandle().destroy()` | Không |
| **Khi nào dùng** | Nhiều bean, optional, strategy pattern, plugin | Lazy đơn giản, lấy instance theo scope |

#### Quy tắc chọn nhanh

- **Chỉ cần lazy 1 bean** -> `Provider<T>` (đơn giản, đủ dùng).
- **Nhiều bean, optional, select runtime** -> `Instance<T>`.
- **Cần destroy @Dependent thủ công** -> `Instance<T>` (bắt buộc).

#### Khi nào KHÔNG nên dùng

- Bean đã là **Normal Scope** (`@ApplicationScoped`, `@RequestScoped`): CDI đã tự Lazy Init qua Client Proxy rồi -> dùng `@Inject` thẳng là đủ, không cần Provider.
- Chỉ có **1 bean duy nhất** và nó **luôn tồn tại** -> `@Inject` trực tiếp đơn giản hơn.

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

### Q2: Instance&lt;T&gt; vs Provider&lt;T&gt;?

- **Provider&lt;T&gt;**: Lazy đơn giản (chỉ `get()`). Dùng khi trì hoãn init 1 bean nặng hoặc lấy instance theo scope hiện tại.
- **Instance&lt;T&gt;**: Nhiều bean (Strategy/Plugin), optional dependency, select qualifier lúc runtime, destroy @Dependent thủ công.
- **Không cần cả hai**: Khi bean là Normal Scope (đã Lazy qua Proxy) và chỉ có 1 implementation -> `@Inject` thẳng.

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
