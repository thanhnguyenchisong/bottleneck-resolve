# Testing - Câu hỏi phỏng vấn Quarkus

## Mục lục
1. [Unit Testing](#unit-testing)
2. [Integration Testing](#integration-testing)
3. [Testcontainers](#testcontainers)
4. [Mocking](#mocking)
5. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## Unit Testing

### Basic Test

```java
// Unit test
@QuarkusTest
class UserResourceTest {
    @Test
    void testGetUser() {
        given()
            .when().get("/users/1")
            .then()
            .statusCode(200)
            .body("id", is(1));
    }
}
```

---

## Integration Testing

### @QuarkusIntegrationTest

```java
// Integration test
@QuarkusIntegrationTest
class UserResourceIT {
    @Test
    void testCreateUser() {
        User user = new User("John", "john@example.com");
        given()
            .contentType(ContentType.JSON)
            .body(user)
            .when().post("/users")
            .then()
            .statusCode(201);
    }
}
```

---

## Testcontainers

### Database Testing

```java
// Testcontainers cho database
@QuarkusTest
@Testcontainers
class UserRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb");
    
    @Test
    void testFindById() {
        // Test với real database
    }
}
```

---

## Mocking

### @Mock

```java
// Mock với @Mock
@QuarkusTest
class UserServiceTest {
    @Mock
    UserRepository userRepository;
    
    @Inject
    UserService userService;
    
    @Test
    void testFindById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        User user = userService.findById(1L);
        assertNotNull(user);
    }
}
```

---

## Câu hỏi thường gặp

### Q1: @QuarkusTest vs @QuarkusIntegrationTest?

```java
// @QuarkusTest:
// - In-process testing
// - Faster
// - Mock support

// @QuarkusIntegrationTest:
// - Full integration
// - Real application
// - Slower
```

---

## Best Practices

1. **Unit tests**: Fast, isolated
2. **Integration tests**: Real components
3. **Testcontainers**: Real databases
4. **Mocking**: Isolate dependencies

---

## Tổng kết

- **Unit Testing**: Fast, isolated
- **Integration Testing**: Full stack
- **Testcontainers**: Real databases
- **Mocking**: Isolate dependencies
- **Best Practices**: Right test type
