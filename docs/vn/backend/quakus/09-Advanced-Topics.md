# Advanced Topics - Câu hỏi phỏng vấn Quarkus

## Mục lục
1. [Security](#security)
2. [Messaging](#messaging)
3. [Monitoring](#monitoring)
4. [Configuration](#configuration)
5. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## Security

### OIDC

```java
// OIDC: OpenID Connect
// Configuration
quarkus.oidc.auth-server-url=http://keycloak:8080/auth/realms/quarkus
quarkus.oidc.client-id=quarkus-app
quarkus.oidc.credentials.secret=secret

// Protect endpoint
@Path("/users")
@RolesAllowed("user")
public class UserResource {
    @GET
    public List<User> getUsers() {
        return userService.findAll();
    }
}
```

### JWT

```java
// JWT: JSON Web Token
// Configuration
mp.jwt.verify.publickey.location=http://keycloak/realms/quarkus/public_key

// Validate JWT
@Path("/users")
public class UserResource {
    @Inject
    JsonWebToken jwt;
    
    @GET
    public List<User> getUsers() {
        String username = jwt.getClaim("preferred_username");
        return userService.findByUsername(username);
    }
}
```

---

## Messaging

### Kafka

```java
// Kafka: Message broker
// Producer
@ApplicationScoped
public class OrderService {
    @Inject
    @Channel("orders")
    Emitter<OrderEvent> orderEmitter;
    
    public void createOrder(Order order) {
        orderRepository.save(order);
        orderEmitter.send(new OrderCreatedEvent(order.getId()));
    }
}

// Consumer
@ApplicationScoped
public class NotificationService {
    @Incoming("orders")
    public void processOrder(OrderEvent event) {
        sendNotification(event.getOrderId());
    }
}
```

---

## Monitoring

### Metrics

```java
// Metrics: Micrometer
@ApplicationScoped
public class UserResource {
    @Inject
    MeterRegistry registry;
    
    @GET
    public List<User> getUsers() {
        Counter counter = registry.counter("users.requested");
        counter.increment();
        return userService.findAll();
    }
}
```

### Health Checks

```java
// Health check
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {
    @Inject
    DataSource dataSource;
    
    @Override
    public HealthCheckResponse call() {
        try {
            dataSource.getConnection();
            return HealthCheckResponse.up("database");
        } catch (Exception e) {
            return HealthCheckResponse.down("database");
        }
    }
}
```

---

## Configuration

### Configuration Properties

```java
// Configuration class
@ConfigMapping(prefix = "app")
public interface AppConfig {
    String name();
    int version();
    DatabaseConfig database();
    
    interface DatabaseConfig {
        String url();
        String username();
    }
}

// Usage
@ApplicationScoped
public class UserService {
    @ConfigProperty(name = "app.name")
    String appName;
    
    @Inject
    AppConfig appConfig;
}
```

---

## Câu hỏi thường gặp

### Q1: Quarkus Security vs Spring Security?

```java
// Quarkus Security:
// - OIDC, JWT built-in
// - Simpler configuration
// - Better performance

// Spring Security:
// - More features
// - More complex
// - Larger ecosystem
```

---

## Best Practices

1. **Use OIDC**: For authentication
2. **JWT**: For stateless auth
3. **Metrics**: Monitor applications
4. **Health checks**: Monitor health
5. **Configuration**: Type-safe config

---

## Tổng kết

- **Security**: OIDC, JWT
- **Messaging**: Kafka, AMQP
- **Monitoring**: Metrics, health checks
- **Configuration**: Type-safe config
- **Best Practices**: Security first, monitor everything
