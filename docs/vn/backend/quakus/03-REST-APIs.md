# REST APIs - Câu hỏi phỏng vấn Quarkus

## Mục lục
1. [RESTEasy Reactive](#resteasy-reactive)
2. [JAX-RS Annotations](#jax-rs-annotations)
3. [JSON Serialization](#json-serialization)
4. [Exception Handling](#exception-handling)
5. [Reactive REST](#reactive-rest)
6. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## RESTEasy Reactive

### What is RESTEasy Reactive?

**RESTEasy Reactive** là JAX-RS implementation của Quarkus, built on top of Vert.x for reactive, non-blocking I/O.

### Basic REST Resource

```java
// REST Resource
@Path("/users")
public class UserResource {
    @Inject
    UserService userService;
    
    @GET
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    @GET
    @Path("/{id}")
    public User getUser(@PathParam("id") Long id) {
        return userService.findById(id);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(User user) {
        User created = userService.create(user);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public User updateUser(@PathParam("id") Long id, User user) {
        return userService.update(id, user);
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(Response.Status.NO_CONTENT)
    public void deleteUser(@PathParam("id") Long id) {
        userService.delete(id);
    }
}
```

---

## JAX-RS Annotations

### Path Annotations

```java
@Path("/api/users")  // Base path
public class UserResource {
    
    @GET
    @Path("/{id}")  // Path parameter
    public User getUser(@PathParam("id") Long id) { }
    
    @GET
    @Path("/search")
    public List<User> searchUsers(@QueryParam("name") String name) { }
    
    @POST
    @Path("/{id}/orders")
    public Order createOrder(@PathParam("id") Long userId, Order order) { }
}
```

### HTTP Methods

```java
@GET      // GET request
@POST     // POST request
@PUT      // PUT request
@DELETE   // DELETE request
@PATCH    // PATCH request
@HEAD     // HEAD request
@OPTIONS  // OPTIONS request
```

### Content Types

```java
@Consumes(MediaType.APPLICATION_JSON)  // Accept JSON input
@Produces(MediaType.APPLICATION_JSON)  // Return JSON output

@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
```

---

## JSON Serialization

### Jackson

```java
// Jackson: Default JSON serializer
// Automatic serialization/deserialization

@Path("/users")
public class UserResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser() {
        return new User(1L, "John", "john@example.com");
        // Automatically serialized to JSON
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(User user) {
        // Automatically deserialized from JSON
        return Response.ok().build();
    }
}
```

### Custom Serialization

```java
// Custom serializer
public class UserSerializer extends JsonSerializer<User> {
    @Override
    public void serialize(User user, JsonGenerator gen, SerializerProvider serializers) {
        gen.writeStartObject();
        gen.writeNumberField("id", user.getId());
        gen.writeStringField("name", user.getName());
        gen.writeEndObject();
    }
}

// Register
@Provider
public class UserSerializerProvider implements ContextResolver<ObjectMapper> {
    @Override
    public ObjectMapper getContext(Class<?> type) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(User.class, new UserSerializer());
        mapper.registerModule(module);
        return mapper;
    }
}
```

---

## Exception Handling

### Exception Mapper

```java
// Exception Mapper
@Provider
public class UserNotFoundExceptionMapper implements ExceptionMapper<UserNotFoundException> {
    @Override
    public Response toResponse(UserNotFoundException exception) {
        ErrorResponse error = new ErrorResponse(
            Response.Status.NOT_FOUND.getStatusCode(),
            exception.getMessage()
        );
        return Response.status(Response.Status.NOT_FOUND).entity(error).build();
    }
}

// Usage
@GET
@Path("/{id}")
public User getUser(@PathParam("id") Long id) {
    return userService.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
}
```

### Global Exception Handler

```java
// Global exception handler
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
        ErrorResponse error = new ErrorResponse(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "Internal server error"
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
}
```

---

## Reactive REST

### Reactive Endpoints

```java
// Reactive: Return Uni (Mutiny)
@Path("/users")
public class UserResource {
    @Inject
    UserService userService;
    
    @GET
    @Path("/{id}")
    public Uni<User> getUser(@PathParam("id") Long id) {
        return userService.findByIdAsync(id);
    }
    
    @GET
    public Multi<User> getAllUsers() {
        return userService.findAllAsync();
    }
    
    @POST
    public Uni<Response> createUser(User user) {
        return userService.createAsync(user)
            .map(created -> Response.status(Response.Status.CREATED).entity(created).build());
    }
}
```

### Uni và Multi

```java
// Uni: Single value (like Mono in Reactor)
Uni<User> user = userService.findByIdAsync(id);

// Multi: Multiple values (like Flux in Reactor)
Multi<User> users = userService.findAllAsync();

// Chaining
Uni<String> result = userService.findByIdAsync(id)
    .map(User::getName)
    .onFailure().recoverWithItem("Unknown");
```

---

## Câu hỏi thường gặp

### Q1: RESTEasy Reactive vs RESTEasy Classic?

```java
// RESTEasy Reactive:
// - Built on Vert.x
// - Non-blocking I/O
// - Better performance
// - Default in Quarkus

// RESTEasy Classic:
// - Traditional blocking
// - Legacy support
// - Use when: Need blocking behavior
```

### Q2: Khi nào dùng Reactive REST?

```java
// Use Reactive khi:
// - High concurrency
// - I/O-bound operations
// - Non-blocking needed
// - Better resource utilization

// Use Blocking khi:
// - CPU-bound operations
// - Simple CRUD
// - Legacy code
```

---

## Best Practices

1. **Use RESTEasy Reactive**: Default, better performance
2. **Proper exception handling**: Exception mappers
3. **Use reactive**: For I/O-bound operations
4. **Validate input**: Use Bean Validation
5. **Document APIs**: OpenAPI/Swagger

---

## Tổng kết

- **RESTEasy Reactive**: Reactive JAX-RS implementation
- **JAX-RS**: Standard annotations
- **JSON**: Jackson serialization
- **Exception Handling**: Exception mappers
- **Reactive**: Uni và Multi
- **Best Practices**: Reactive first, proper error handling
