# Testing - Câu hỏi phỏng vấn Quarkus

## Mục lục
1. [Unit Testing (@QuarkusTest)](#unit-testing)
2. [Mocking (@InjectMock)](#mocking)
3. [Dev Services (Zero Config)](#dev-services)
4. [Test Profiles](#test-profiles)
5. [Integration Testing](#integration-testing)
6. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

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

## Mocking (@InjectMock)

### @InjectMock (Mockito)

```java
@QuarkusTest
public class UserServiceTest {

    @InjectMock
    UserRepository userRepository; // Tự động tạo Mockito mock và inject vào bean

    @Inject
    UserService userService;

    @Test
    public void test() {
        Mockito.when(userRepository.count()).thenReturn(10L);
        Assertions.assertEquals(10L, userService.getUserCount());
    }
}
```

### QuarkusMock (Programmatic)

```java
// Mocking static methods hoặc object không quản lý bởi CDI
QuarkusMock.installMockForType(new MockExternalService(), ExternalService.class);
```

---

## Dev Services (Zero Config)

Quarkus tự động start Database, Kafka, Redis... bằng Testcontainers mà **không cần config** gì cả.

- Chỉ cần dependency (VD: `quarkus-jdbc-postgresql`).
- Không khai báo URL trong `application.properties`.
- Quarkus sẽ tự pull image, start container và wire cấu hình vào app.

```bash
# Tắt Dev Services nếu muốn dùng DB thật
quarkus.datasource.devservices.enabled=false
```

---

## Test Profiles

Chạy test với các cấu hình khác nhau.

```java
public class MockProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("quarkus.hibernate-orm.database.generation", "drop-and-create");
    }
}

@QuarkusTest
@TestProfile(MockProfile.class)
public class ProfileTest { ... }
```

---

## Integration Testing

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
