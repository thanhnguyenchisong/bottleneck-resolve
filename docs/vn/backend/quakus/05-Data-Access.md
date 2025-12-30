# Data Access - Câu hỏi phỏng vấn Quarkus

## Mục lục
1. [Hibernate ORM](#hibernate-orm)
2. [Panache](#panache)
3. [Reactive SQL Clients](#reactive-sql-clients)
4. [Transactions](#transactions)
5. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

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

## Panache

### Panache Entity

```java
// Panache: Simplifies data access
@Entity
public class User extends PanacheEntity {
    public String username;
    public String email;
    
    // No need for getters/setters
    // Panache provides them
}

// Usage
User user = User.findById(1L);
List<User> users = User.listAll();
User user = User.find("username", "john").firstResult();
```

### Panache Repository

```java
// Panache Repository
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public User findByUsername(String username) {
        return find("username", username).firstResult();
    }
    
    public List<User> findActiveUsers() {
        return find("active", true).list();
    }
}

// Usage
@Inject
UserRepository userRepository;

User user = userRepository.findByUsername("john");
```

---

## Reactive SQL Clients

### Reactive PostgreSQL Client

```java
// Reactive SQL Client
@ApplicationScoped
public class UserRepository {
    @Inject
    ReactiveSqlClient client;
    
    public Uni<List<User>> findAll() {
        return client.query("SELECT * FROM users")
            .mapping(User::from)
            .collect().asList();
    }
    
    public Uni<User> findById(Long id) {
        return client.query("SELECT * FROM users WHERE id = $1", id)
            .mapping(User::from)
            .collect().first();
    }
}
```

---

## Transactions

### @Transactional

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
