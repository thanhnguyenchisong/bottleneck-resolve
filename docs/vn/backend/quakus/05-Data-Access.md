# Data Access - Câu hỏi phỏng vấn Quarkus

## Mục lục
1. [Hibernate ORM](#hibernate-orm)
2. [Panache (Repository vs Active Record)](#panache-repository-vs-active-record)
3. [Projections & Locking](#projections-&-locking)
4. [Hibernate Reactive](#hibernate-reactive)
5. [Transactions & Multi-tenancy](#transactions-&-multi-tenancy)
6. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## Hibernate ORM

### Basic Entity

```java
// Entity với Hibernate ORM
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String email;
    
    // Getters and setters
}
```

### EntityManager

```java
// EntityManager injection
@ApplicationScoped
public class UserRepository {
    @PersistenceContext
    EntityManager em;
    
    public User findById(Long id) {
        return em.find(User.class, id);
    }
    
    public void save(User user) {
        em.persist(user);
    }
}
```

---

## Panache (Repository vs Active Record)

### Active Record Pattern
Entity tự quản lý persistence của chính nó.
- **Ưu điểm**: Ngắn gọn, code nằm cùng dữ liệu.
- **Nhược điểm**: Entity bị couple với logic truy xuất data, khó mock/test riêng biệt, vi phạm Single Responsibility Principle nếu quá phức tạp.

```java
@Entity
public class User extends PanacheEntity {
    public String name;
    
    // Logic business trên entity
    public static User findByName(String name) {
        return find("name", name).firstResult();
    }
}
// Usage: User.findByName("Alice");
```

### Repository Pattern
Tách biệt logic truy xuất data ra khỏi entity.
- **Ưu điểm**: Tách biệt rõ ràng (Clean Architecture), dễ mock test, linh hoạt đổi implementation.
- **Nhược điểm**: Boilerplate code (cần tạo class Repository).

```java
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public User findByName(String name) {
        return find("name", name).firstResult();
    }
}
// Usage: repo.findByName("Alice");
```

### Khi nào dùng cái nào?
- **Active Record**: CRUD đơn giản, prototype nhanh.
- **Repository**: Dự án lớn, logic phức tạp, cần clean architecture.

---

## Projections & Locking

### Projections (DTOs)
Tránh fetch dư thừa column, tăng hiệu năng (chỉ select field cần thiết).

```java
// DTO (Java Record hoặc Class)
public record UserSummary(String name, String email) {}

// Query projection
List<UserSummary> summaries = User.find("active", true)
    .project(UserSummary.class)
    .list();
// SQL sinh ra chỉ select name, email
```

### Locking
Xử lý concurrency.

```java
// Pessimistic Lock (SELECT FOR UPDATE)
User.find("id", 1L, LockModeType.PESSIMISTIC_WRITE).firstResult();

// Optimistic Lock (Versioning)
@Version
public int version; // Tự động check version khi update
```

---

## Hibernate Reactive

Sử dụng Panache với non-blocking driver (Postgres/MySQL Reactive).

```java
@Entity
public class User extends PanacheEntity { // Reactive PanacheEntity
    public String name;
}

// Trả về Uni/Multi thay vì Object/List
@GET
public Uni<User> get(Long id) {
    return User.<User>findById(id); // Non-blocking
}

@POST
public Uni<Response> create(User user) {
    return Panache.withTransaction(user::persist) // Transaction reactive
        .map(v -> Response.ok(user).build());
}
```

---

## Transactions & Multi-tenancy

### Programmatic Transaction
Kiểm soát transaction bằng code thay vì annotation.

```java
QuarkusTransaction.begin();
try {
    // DB operations
    QuarkusTransaction.commit();
} catch (Exception e) {
    QuarkusTransaction.rollback();
}
```

### Multi-tenancy (Đa người dùng)
Hỗ trợ Database-per-tenant hoặc Schema-per-tenant.

```properties
# Schema approach
quarkus.hibernate-orm.multitenant=SCHEMA
```

```java
// Resolver tenant từ request (header, jwt)
@PersistenceUnitExtension
@RequestScoped
public class CustomTenantResolver implements TenantResolver {
    @Override
    public String resolveTenantId() {
        return "tenant_1"; // Lấy từ context
    }
}
```

---

### @Transactional

```java
// Declarative Transaction
// REQUIRED (default): Join existing or create new
@Transactional
public void doSomething() { ... }

// REQUIRES_NEW: Always new transaction
@Transactional(TxType.REQUIRES_NEW)
public void doIndependent() { ... }
```

```java
// Transactional method
@ApplicationScoped
public class UserService {
    @Inject
    UserRepository userRepository;
    
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    @Transactional(rollbackOn = Exception.class)
    public void updateUser(Long id, User user) {
        User existing = userRepository.findById(id);
        existing.setUsername(user.getUsername());
        userRepository.save(existing);
    }
}
```

---

## Câu hỏi thường gặp

### Q1: Panache vs JPA?

```java
// Panache:
// - Simpler API
// - Less boilerplate
// - Active record pattern
// - Type-safe queries

// JPA:
// - Standard
// - More control
// - Traditional approach
```

---

## Best Practices

1. **Use Panache**: Simpler, less boilerplate
2. **Type-safe queries**: Panache queries
3. **Transactions**: Use @Transactional
4. **Reactive**: For high concurrency

---

## Tổng kết

- **Hibernate ORM**: Standard JPA
- **Panache**: Simplified data access
- **Reactive SQL**: Non-blocking database access
- **Transactions**: @Transactional
- **Best Practices**: Use Panache, type-safe queries
