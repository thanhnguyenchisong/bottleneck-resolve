# Performance Optimization - Câu hỏi phỏng vấn JPA

## Mục lục
1. [N+1 Problem](#n1-problem)
2. [Lazy vs Eager Loading](#lazy-vs-eager-loading)
3. [Batch Operations](#batch-operations)
4. [Caching](#caching)
5. [Query Optimization](#query-optimization)
6. [Connection Pooling](#connection-pooling)
7. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## N+1 Problem

### Problem Definition

**N+1 Problem:** Khi load N entities, thực hiện 1 query cho parent entities và N queries cho child entities.

### Example

```java
// Problem: N+1 queries
List<User> users = userRepository.findAll();  // Query 1: SELECT * FROM users

for (User user : users) {
    List<Order> orders = user.getOrders();  // Query 2, 3, 4, ... N
    // SELECT * FROM orders WHERE user_id = 1
    // SELECT * FROM orders WHERE user_id = 2
    // ...
}
// Total: 1 + N queries
```

### Solutions

#### Solution 1: JOIN FETCH

```java
// JPQL với JOIN FETCH
@Query("SELECT DISTINCT u FROM User u JOIN FETCH u.orders")
List<User> findAllWithOrders();

// Native query
@Query(value = "SELECT u.*, o.* FROM users u LEFT JOIN orders o ON u.id = o.user_id",
       nativeQuery = true)
List<User> findAllWithOrdersNative();
```

#### Solution 2: EntityGraph

```java
// Define EntityGraph
@Entity
@NamedEntityGraph(
    name = "User.withOrders",
    attributeNodes = @NamedAttributeNode("orders")
)
public class User {
    @OneToMany
    private List<Order> orders;
}

// Use EntityGraph
@EntityGraph("User.withOrders")
List<User> findAll();

// Or inline
@EntityGraph(attributePaths = {"orders", "profile"})
Optional<User> findById(Long id);
```

#### Solution 3: @BatchSize

```java
@Entity
public class User {
    @OneToMany
    @BatchSize(size = 10)  // Load 10 users' orders at once
    private List<Order> orders;
}

// Instead of N queries, uses: N/10 queries
```

#### Solution 4: Fetch Join trong Criteria API

```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<User> query = cb.createQuery(User.class);
Root<User> user = query.from(User.class);
user.fetch("orders", JoinType.LEFT);
query.distinct(true);
List<User> users = em.createQuery(query).getResultList();
```

---

## Lazy vs Eager Loading

### Lazy Loading

```java
@Entity
public class User {
    @OneToMany(fetch = FetchType.LAZY)
    private List<Order> orders;  // Loaded when accessed
}

// Usage
User user = userRepository.findById(1L).orElse(null);
// Orders not loaded yet

List<Order> orders = user.getOrders();  // Loaded here (if in transaction)
```

**Pros:**
- Load only what you need
- Better initial performance
- Less memory usage

**Cons:**
- LazyInitializationException if outside transaction
- Multiple queries if not using JOIN FETCH

### Eager Loading

```java
@Entity
public class User {
    @OneToMany(fetch = FetchType.EAGER)
    private List<Order> orders;  // Loaded immediately
}

// Usage
User user = userRepository.findById(1L).orElse(null);
// Orders already loaded
List<Order> orders = user.getOrders();  // No additional query
```

**Pros:**
- No LazyInitializationException
- Data available immediately

**Cons:**
- Loads data you might not need
- Can cause N+1 problem
- Slower initial load

### Best Practices

```java
// ✅ Good: LAZY for @OneToMany
@OneToMany(fetch = FetchType.LAZY)
private List<Order> orders;

// ✅ Good: LAZY for @ManyToMany
@ManyToMany(fetch = FetchType.LAZY)
private Set<Course> courses;

// ✅ Good: LAZY for @ManyToOne (usually)
@ManyToOne(fetch = FetchType.LAZY)
private User user;

// ⚠️ Consider: EAGER for @OneToOne (if always needed)
@OneToOne(fetch = FetchType.EAGER)
private UserProfile profile;
```

---

## Batch Operations

### Batch Insert

```java
// ❌ Bad: Multiple inserts
for (User user : users) {
    userRepository.save(user);  // One INSERT per user
}
// 1000 users = 1000 INSERT statements

// ✅ Good: Batch insert
@Service
@Transactional
public class UserService {
    @PersistenceContext
    private EntityManager em;
    
    public void saveUsersBatch(List<User> users) {
        int batchSize = 50;
        for (int i = 0; i < users.size(); i++) {
            em.persist(users.get(i));
            if (i % batchSize == 0 && i > 0) {
                em.flush();
                em.clear();
            }
        }
    }
}

// Configuration
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

### Batch Update

```java
// ❌ Bad: Multiple updates
for (User user : users) {
    user.setActive(true);
    userRepository.save(user);  // One UPDATE per user
}

// ✅ Good: Batch update với @Query
@Modifying
@Query("UPDATE User u SET u.active = :active WHERE u.id IN :ids")
void updateActiveStatus(@Param("ids") List<Long> ids, @Param("active") Boolean active);

// ✅ Good: Batch update với EntityManager
@Service
@Transactional
public class UserService {
    @PersistenceContext
    private EntityManager em;
    
    public void updateUsersBatch(List<User> users) {
        int batchSize = 50;
        for (int i = 0; i < users.size(); i++) {
            em.merge(users.get(i));
            if (i % batchSize == 0 && i > 0) {
                em.flush();
                em.clear();
            }
        }
    }
}
```

### Batch Delete

```java
// ❌ Bad: Multiple deletes
for (User user : users) {
    userRepository.delete(user);  // One DELETE per user
}

// ✅ Good: Batch delete với @Query
@Modifying
@Query("DELETE FROM User u WHERE u.id IN :ids")
void deleteByIds(@Param("ids") List<Long> ids);

// ✅ Good: Delete in batch
@Service
@Transactional
public class UserService {
    @PersistenceContext
    private EntityManager em;
    
    public void deleteUsersBatch(List<Long> ids) {
        int batchSize = 50;
        for (int i = 0; i < ids.size(); i++) {
            User user = em.getReference(User.class, ids.get(i));
            em.remove(user);
            if (i % batchSize == 0 && i > 0) {
                em.flush();
                em.clear();
            }
        }
    }
}
```

---

## Caching

### First-Level Cache (Persistence Context)

```java
// Automatic first-level cache
EntityManager em = emf.createEntityManager();

User user1 = em.find(User.class, 1L);  // Query database
User user2 = em.find(User.class, 1L);  // Return from cache (no query)

assert user1 == user2;  // Same instance
```

### Second-Level Cache

```java
// Enable second-level cache
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User {
    // ...
}

// Configuration
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory

// Usage
@Cacheable("users")
public User findById(Long id) {
    return userRepository.findById(id).orElse(null);
}

@CacheEvict(value = "users", key = "#id")
public void deleteUser(Long id) {
    userRepository.deleteById(id);
}

@CachePut(value = "users", key = "#user.id")
public User updateUser(User user) {
    return userRepository.save(user);
}
```

### Query Cache

```java
// Enable query cache
@QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
@Query("SELECT u FROM User u WHERE u.active = true")
List<User> findActiveUsers();

// Configuration
spring.jpa.properties.hibernate.cache.use_query_cache=true
```

---

## Query Optimization

### Select Only Needed Columns

```java
// ❌ Bad: Select all columns
@Query("SELECT u FROM User u")
List<User> findAll();

// ✅ Good: Select only needed columns
@Query("SELECT u.id, u.username, u.email FROM User u")
List<Object[]> findUserSummaries();

// ✅ Good: Use projection
public interface UserSummary {
    String getUsername();
    String getEmail();
}

@Query("SELECT u.username as username, u.email as email FROM User u")
List<UserSummary> findUserSummaries();
```

### Use Indexes

```java
@Entity
@Table(indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_age_city", columnList = "age, city")
})
public class User {
    private String username;
    private String email;
    private Integer age;
    private String city;
}
```

### Avoid SELECT N+1

```java
// ❌ Bad: N+1 queries
List<User> users = userRepository.findAll();
users.forEach(user -> user.getOrders().size());

// ✅ Good: JOIN FETCH
@Query("SELECT DISTINCT u FROM User u JOIN FETCH u.orders")
List<User> findAllWithOrders();
```

### Use Pagination

```java
// ❌ Bad: Load all records
List<User> users = userRepository.findAll();  // Loads all

// ✅ Good: Pagination
Page<User> users = userRepository.findAll(PageRequest.of(0, 20));
```

---

## Connection Pooling

### HikariCP Configuration

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

### Connection Pool Sizing

```java
// Formula:
// connections = ((core_count * 2) + effective_spindle_count)

// Example:
// 4 cores, 1 database
// connections = (4 * 2) + 1 = 9

// For read replicas:
// connections = (4 * 2) + 1 = 9 per database
// Total = 9 * 2 = 18 (if 2 databases)
```

---

## Câu hỏi thường gặp

### Q1: Làm sao detect N+1 problem?

```java
// Enable SQL logging
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

// Check logs for multiple similar queries
// SELECT * FROM users
// SELECT * FROM orders WHERE user_id = 1
// SELECT * FROM orders WHERE user_id = 2
// ...

// Use Hibernate statistics
spring.jpa.properties.hibernate.generate_statistics=true
// Check: query execution count
```

### Q2: Khi nào dùng EAGER vs LAZY?

**Use LAZY:**
- Collections (@OneToMany, @ManyToMany)
- Optional relationships
- Large datasets

**Use EAGER:**
- Always needed data
- Small datasets
- @OneToOne (sometimes)

### Q3: Batch size optimization?

```java
// Too small: Many round trips
batch_size = 10  // 1000 records = 100 batches

// Too large: Memory issues
batch_size = 10000  // May cause OutOfMemoryError

// Optimal: 20-50
batch_size = 50  // Good balance
```

### Q4: Caching strategy?

```java
// READ_ONLY: Immutable data
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)

// READ_WRITE: Read and write
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

// NONSTRICT_READ_WRITE: Loose consistency
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

// TRANSACTIONAL: Full ACID
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
```

---

## Best Practices

1. **Always use JOIN FETCH** để tránh N+1
2. **Use LAZY fetching** cho collections
3. **Enable batch operations** cho bulk inserts/updates
4. **Use pagination** cho large result sets
5. **Select only needed columns** với projections
6. **Enable second-level cache** cho read-heavy data
7. **Configure connection pool** appropriately
8. **Monitor query performance** với statistics
9. **Use indexes** trên frequently queried columns
10. **Avoid EAGER on collections** (causes N+1)

---

## Bài tập thực hành

### Bài 1: Fix N+1 Problem

```java
// Yêu cầu:
// 1. Identify N+1 problem trong code
// 2. Fix bằng JOIN FETCH
// 3. Fix bằng EntityGraph
// 4. Compare performance
```

### Bài 2: Batch Operations

```java
// Yêu cầu: Implement batch insert/update/delete
// 1. Insert 10,000 users efficiently
// 2. Update 10,000 users efficiently
// 3. Delete 10,000 users efficiently
```

---

## Tổng kết

- **N+1 Problem**: Multiple queries instead of one
- **Solutions**: JOIN FETCH, EntityGraph, @BatchSize
- **Lazy vs Eager**: LAZY for collections, EAGER when needed
- **Batch Operations**: Configure batch size, use flush/clear
- **Caching**: First-level, second-level, query cache
- **Query Optimization**: Select needed columns, use indexes, pagination
- **Connection Pooling**: Proper sizing and configuration
- **Best Practices**: Monitor, optimize, test performance
