# Spring Framework - Câu hỏi phỏng vấn Java

## Mục lục
1. [Spring Core Concepts](#spring-core-concepts)
2. [Dependency Injection (DI)](#dependency-injection-di)
3. [IoC Container](#ioc-container)
4. [Bean Scopes](#bean-scopes)
5. [Annotations](#annotations)
6. [AOP (Aspect-Oriented Programming)](#aop-aspect-oriented-programming)
7. [Spring MVC](#spring-mvc)
8. [Spring Boot](#spring-boot)
9. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## Spring Core Concepts

### Spring Framework là gì?

Spring là một **lightweight, inversion of control (IoC)** và **aspect-oriented (AOP)** framework cho Java.

### Core Features

- **Dependency Injection (DI)**
- **Inversion of Control (IoC)**
- **Aspect-Oriented Programming (AOP)**
- **MVC Framework**
- **Transaction Management**
- **Exception Handling**

### Spring Modules

```
Spring Core
├── Spring Context (Application Context)
├── Spring AOP
├── Spring DAO
├── Spring ORM
├── Spring Web
├── Spring MVC
└── Spring Test
```

---

## Dependency Injection (DI)

### Vấn đề: Tight Coupling

```java
// ❌ Bad: Tight coupling
class UserService {
    private UserRepository userRepository = new UserRepositoryImpl();
    
    public User getUser(Long id) {
        return userRepository.findById(id);
    }
}
```

### Giải pháp: Dependency Injection

```java
// ✅ Good: Loose coupling với DI
class UserService {
    private UserRepository userRepository;
    
    // Constructor Injection
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User getUser(Long id) {
        return userRepository.findById(id);
    }
}
```

### Types of DI

#### 1. Constructor Injection (Recommended)

```java
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

#### 2. Setter Injection

```java
@Service
public class UserService {
    private UserRepository userRepository;
    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

#### 3. Field Injection

```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

**Lưu ý:** Field injection không recommended vì khó test và không thể make field final.

---

## IoC Container

### ApplicationContext vs BeanFactory

**BeanFactory:**
- Basic IoC container
- Lazy loading
- Lightweight

**ApplicationContext:**
- Extends BeanFactory
- Eager loading
- Additional features (AOP, events, i18n)

### Configuration

#### 1. XML Configuration

```xml
<!-- applicationContext.xml -->
<beans>
    <bean id="userRepository" class="com.example.UserRepositoryImpl"/>
    <bean id="userService" class="com.example.UserService">
        <constructor-arg ref="userRepository"/>
    </bean>
</beans>
```

```java
ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
UserService userService = context.getBean(UserService.class);
```

#### 2. Java Configuration

```java
@Configuration
public class AppConfig {
    @Bean
    public UserRepository userRepository() {
        return new UserRepositoryImpl();
    }
    
    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserService(userRepository);
    }
}
```

```java
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
UserService userService = context.getBean(UserService.class);
```

#### 3. Component Scanning (Recommended)

```java
@Configuration
@ComponentScan(basePackages = "com.example")
public class AppConfig {
}

// Or in Spring Boot
@SpringBootApplication  // Includes @ComponentScan
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## Bean Scopes

### Singleton (Default)

```java
@Component
@Scope("singleton")  // Default
public class UserService {
    // One instance per Spring container
}
```

### Prototype

```java
@Component
@Scope("prototype")
public class UserService {
    // New instance mỗi lần request
}
```

### Request (Web)

```java
@Component
@Scope("request")
public class UserService {
    // One instance per HTTP request
}
```

### Session (Web)

```java
@Component
@Scope("session")
public class UserService {
    // One instance per HTTP session
}
```

### Application (Web)

```java
@Component
@Scope("application")
public class UserService {
    // One instance per ServletContext
}
```

### WebSocket (Web)

```java
@Component
@Scope("websocket")
public class UserService {
    // One instance per WebSocket session
}
```

---

## Annotations

### Core Annotations

#### @Component

```java
@Component
public class UserService {
    // Generic Spring component
}
```

#### @Service

```java
@Service
public class UserService {
    // Service layer component
}
```

#### @Repository

```java
@Repository
public class UserRepositoryImpl implements UserRepository {
    // Data access layer component
    // Exception translation enabled
}
```

#### @Controller

```java
@Controller
public class UserController {
    // Web layer component
}
```

#### @Autowired

```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    // Constructor injection (preferred)
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

#### @Qualifier

```java
interface PaymentService {}

@Service("creditCard")
class CreditCardService implements PaymentService {}

@Service("paypal")
class PayPalService implements PaymentService {}

@Service
class OrderService {
    @Autowired
    @Qualifier("creditCard")
    private PaymentService paymentService;
}
```

#### @Primary

```java
@Service
@Primary
class DefaultPaymentService implements PaymentService {}

@Service
class OrderService {
    @Autowired
    private PaymentService paymentService;  // Uses DefaultPaymentService
}
```

#### @Value

```java
@Component
public class AppConfig {
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.version:1.0.0}")  // Default value
    private String appVersion;
    
    @Value("#{systemProperties['java.home']}")
    private String javaHome;
}
```

#### @Configuration và @Bean

```java
@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource();
    }
    
    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
    public CacheManager cacheManager() {
        return new SimpleCacheManager();
    }
}
```

#### @Profile

```java
@Configuration
@Profile("dev")
public class DevConfig {
    // Configuration for development
}

@Configuration
@Profile("prod")
public class ProdConfig {
    // Configuration for production
}
```

---

## AOP (Aspect-Oriented Programming)

### Concepts

- **Aspect**: Module cross-cutting concerns
- **Join Point**: Point trong execution (method call, exception)
- **Pointcut**: Expression để match join points
- **Advice**: Action tại join point
- **Weaving**: Apply aspects to objects

### Types of Advice

- **Before**: Execute trước method
- **After**: Execute sau method (success hoặc failure)
- **AfterReturning**: Execute sau method return successfully
- **AfterThrowing**: Execute sau method throw exception
- **Around**: Execute before và after method

### Example

```java
// Aspect
@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    
    // Pointcut: All methods in service package
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void serviceMethods() {}
    
    // Before advice
    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Before: " + joinPoint.getSignature().getName());
    }
    
    // After returning
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("After returning: " + result);
    }
    
    // After throwing
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        logger.error("Exception: " + exception.getMessage());
    }
    
    // Around advice
    @Around("serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        logger.info("Execution time: " + executionTime + "ms");
        return result;
    }
}
```

### Pointcut Expressions

```java
// Execution
@Pointcut("execution(* com.example.service.*.*(..))")
// Return type: *, Package: com.example.service, Method: *, Parameters: (..)

// Within
@Pointcut("within(com.example.service..*)")
// All methods in service package and subpackages

// This
@Pointcut("this(com.example.service.UserService)")
// All methods in UserService

// Target
@Pointcut("target(com.example.service.UserService)")
// All methods implementing UserService

// Args
@Pointcut("args(Long, String)")
// Methods with Long and String parameters

// Annotation
@Pointcut("@annotation(com.example.annotation.Logged)")
// Methods annotated with @Logged
```

---

## Spring MVC

### Architecture

```
Client Request
    ↓
DispatcherServlet
    ↓
HandlerMapping → Controller
    ↓
ModelAndView
    ↓
ViewResolver → View
    ↓
Response
```

### Controller

```java
@Controller
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // GET /users
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users/list";
    }
    
    // GET /users/{id}
    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        return "users/detail";
    }
    
    // POST /users
    @PostMapping
    public String createUser(@ModelAttribute User user) {
        userService.save(user);
        return "redirect:/users";
    }
    
    // PUT /users/{id}
    @PutMapping("/{id}")
    @ResponseBody
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.update(id, user);
    }
    
    // DELETE /users/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
```

### REST Controller

```java
@RestController
@RequestMapping("/api/users")
public class UserRestController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid User user) {
        User created = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

---

## Spring Boot

### Features

- **Auto-configuration**
- **Starter dependencies**
- **Embedded server**
- **Production-ready features**

### Main Annotation

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

`@SpringBootApplication` includes:
- `@Configuration`
- `@EnableAutoConfiguration`
- `@ComponentScan`

### Application Properties

```properties
# application.properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

```yaml
# application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
```

### Profiles

```properties
# application-dev.properties
spring.datasource.url=jdbc:h2:mem:testdb

# application-prod.properties
spring.datasource.url=jdbc:mysql://prod-server:3306/mydb
```

```bash
# Run with profile
java -jar app.jar --spring.profiles.active=prod
```

---

## Câu hỏi thường gặp

### Q1: Sự khác biệt giữa @Component, @Service, @Repository, @Controller?

Tất cả đều là `@Component`, nhưng có semantic meaning:
- `@Component`: Generic component
- `@Service`: Business logic layer
- `@Repository`: Data access layer (exception translation)
- `@Controller`: Web layer (MVC)

### Q2: @Autowired vs @Resource vs @Inject?

- `@Autowired`: Spring-specific, by type, có thể dùng với `@Qualifier`
- `@Resource`: JSR-250, by name, sau đó by type
- `@Inject`: JSR-330, by type, giống `@Autowired`

### Q3: Singleton scope trong Spring vs Singleton pattern?

- **Spring Singleton**: One instance per Spring container (có thể có nhiều containers)
- **Singleton Pattern**: One instance per JVM

### Q4: Circular Dependency?

```java
// Circular dependency
@Component
class A {
    @Autowired
    private B b;
}

@Component
class B {
    @Autowired
    private A a;
}
```

**Solutions:**
- Use `@Lazy`
- Refactor code (extract common logic)
- Use setter injection

### Q5: Bean Lifecycle?

```
1. Instantiate
2. Populate properties
3. BeanNameAware.setBeanName()
4. BeanFactoryAware.setBeanFactory()
5. ApplicationContextAware.setApplicationContext()
6. @PostConstruct
7. InitializingBean.afterPropertiesSet()
8. Custom init method
9. Bean ready
10. @PreDestroy
11. DisposableBean.destroy()
12. Custom destroy method
```

### Q6: Transaction Management?

```java
@Service
@Transactional
public class UserService {
    
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public User save(User user) {
        return userRepository.save(user);
    }
}
```

---

## Best Practices

1. **Use constructor injection** thay vì field injection
2. **Prefer interfaces** cho dependencies
3. **Use @Service, @Repository, @Controller** thay vì generic @Component
4. **Avoid circular dependencies**
5. **Use @Transactional** appropriately
6. **Profile-specific configurations**
7. **Use @ConfigurationProperties** cho complex properties
8. **Test với @SpringBootTest**

---

## Bài tập thực hành

### Bài 1: Implement Service Layer

```java
// Yêu cầu: Tạo UserService với DI
// - Inject UserRepository
// - Implement CRUD operations
// - Add transaction management
```

### Bài 2: AOP Logging

```java
// Yêu cầu: Tạo aspect để log tất cả service methods
// - Log method name, parameters, return value
// - Log execution time
```

### Bài 3: Configuration Properties

```java
// Yêu cầu: Tạo @ConfigurationProperties cho database config
// - Load từ application.properties
// - Validate properties
```

---

## Advanced Spring Topics

### Bean Lifecycle Callbacks

```java
@Component
public class MyBean implements InitializingBean, DisposableBean {
    
    // Method 1: @PostConstruct và @PreDestroy
    @PostConstruct
    public void init() {
        System.out.println("Bean initialized");
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("Bean destroyed");
    }
    
    // Method 2: InitializingBean và DisposableBean interfaces
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("After properties set");
    }
    
    @Override
    public void destroy() throws Exception {
        System.out.println("Bean destroyed");
    }
    
    // Method 3: Custom init/destroy methods
    public void customInit() {
        System.out.println("Custom init");
    }
    
    public void customDestroy() {
        System.out.println("Custom destroy");
    }
}

// Configuration
@Configuration
public class AppConfig {
    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public MyBean myBean() {
        return new MyBean();
    }
}
```

### Bean Post Processors

```java
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("Before initialization: " + beanName);
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("After initialization: " + beanName);
        // Can return proxy
        return bean;
    }
}
```

### Conditional Bean Creation

```java
@Configuration
public class ConditionalConfig {
    
    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
    public CacheManager cacheManager() {
        return new SimpleCacheManager();
    }
    
    @Bean
    @ConditionalOnClass(name = "com.example.ExternalService")
    public ExternalService externalService() {
        return new ExternalService();
    }
    
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager defaultCacheManager() {
        return new NoOpCacheManager();
    }
    
    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        return new H2DataSource();
    }
    
    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        return new MySQLDataSource();
    }
}
```

### @ConfigurationProperties

```java
@ConfigurationProperties(prefix = "app")
@Component
public class AppProperties {
    private String name;
    private String version;
    private Database database;
    
    // Getters and setters
    
    public static class Database {
        private String url;
        private String username;
        private String password;
        
        // Getters and setters
    }
}

// application.properties
app.name=MyApp
app.version=1.0.0
app.database.url=jdbc:mysql://localhost:3306/mydb
app.database.username=root
app.database.password=password
```

### Event Publishing

```java
// Event class
public class UserCreatedEvent extends ApplicationEvent {
    private String username;
    
    public UserCreatedEvent(Object source, String username) {
        super(source);
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
}

// Publisher
@Service
public class UserService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public void createUser(User user) {
        // Create user
        userRepository.save(user);
        
        // Publish event
        eventPublisher.publishEvent(new UserCreatedEvent(this, user.getUsername()));
    }
}

// Listener
@Component
public class UserEventListener {
    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        System.out.println("User created: " + event.getUsername());
        // Send email, create audit log, etc.
    }
    
    @Async
    @EventListener
    public void handleUserCreatedAsync(UserCreatedEvent event) {
        // Async processing
    }
}
```

### Spring Expression Language (SpEL)

```java
@Component
public class SpELExample {
    @Value("#{systemProperties['java.home']}")
    private String javaHome;
    
    @Value("#{T(java.lang.Math).random() * 100}")
    private double randomNumber;
    
    @Value("#{userService.getDefaultUser().username}")
    private String defaultUsername;
    
    @Value("#{users.size() > 0 ? users[0] : null}")
    private User firstUser;
    
    @Value("#{users.?[age > 18]}")
    private List<User> adults;
    
    @Value("#{users.![username]}")
    private List<String> usernames;
}
```

### Transaction Management - Advanced

```java
@Service
public class TransactionService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionService self;  // For self-invocation
    
    // Propagation: REQUIRED (default)
    @Transactional
    public void method1() {
        userRepository.save(new User());
        self.method2();  // Joins existing transaction
    }
    
    // Propagation: REQUIRES_NEW
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void method2() {
        // New transaction, independent of method1
        userRepository.save(new User());
    }
    
    // Isolation: READ_COMMITTED
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public User findUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    // Timeout
    @Transactional(timeout = 5)
    public void longRunningOperation() {
        // Fails if takes more than 5 seconds
    }
    
    // Rollback rules
    @Transactional(rollbackFor = {SQLException.class, IOException.class})
    public void saveWithRollback() {
        // Rolls back on SQLException or IOException
    }
    
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void saveWithoutRollback() {
        // Doesn't rollback on IllegalArgumentException
    }
}
```

### AOP - Advanced Pointcuts

```java
@Aspect
@Component
public class AdvancedAspect {
    
    // Execution pointcut
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void serviceMethods() {}
    
    // Within pointcut
    @Pointcut("within(com.example.service..*)")
    public void inServicePackage() {}
    
    // This pointcut
    @Pointcut("this(com.example.service.UserService)")
    public void userServiceMethods() {}
    
    // Target pointcut
    @Pointcut("target(com.example.service.UserService)")
    public void userServiceTarget() {}
    
    // Args pointcut
    @Pointcut("args(Long, String)")
    public void methodsWithLongAndString() {}
    
    // Annotation pointcut
    @Pointcut("@annotation(com.example.annotation.Logged)")
    public void loggedMethods() {}
    
    // Bean pointcut
    @Pointcut("bean(userService)")
    public void userServiceBean() {}
    
    // Combined pointcuts
    @Around("serviceMethods() && !loggedMethods()")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        // Advice logic
        return joinPoint.proceed();
    }
}
```

### Spring Boot Actuator

```java
// Dependencies
// spring-boot-starter-actuator

// Endpoints
// /actuator/health - Application health
// /actuator/info - Application information
// /actuator/metrics - Application metrics
// /actuator/env - Environment properties
// /actuator/beans - All Spring beans

// Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

## Advanced Interview Questions

### Q1: Bean Scopes - Khi nào dùng gì?

```java
// Singleton (Default): One instance per Spring container
@Service
@Scope("singleton")
public class UserService {
    // Shared state across all requests
    // Use for stateless services
}

// Prototype: New instance mỗi lần request
@Component
@Scope("prototype")
public class UserValidator {
    // New instance each time
    // Use when need fresh state
}

// Request: One instance per HTTP request
@Component
@Scope("request")
public class RequestScopedBean {
    // Use for request-specific data
}

// Session: One instance per HTTP session
@Component
@Scope("session")
public class SessionScopedBean {
    // Use for session-specific data
}
```

### Q2: @Autowired vs @Resource vs @Inject?

```java
// @Autowired: Spring-specific, by type, then by name
@Autowired
private UserRepository userRepository;  // By type

@Autowired
@Qualifier("userRepositoryImpl")
private UserRepository userRepository;  // By name with qualifier

// @Resource: JSR-250, by name, then by type
@Resource(name = "userRepository")
private UserRepository userRepository;  // By name

// @Inject: JSR-330, by type (like @Autowired)
@Inject
private UserRepository userRepository;  // By type
```

### Q3: Circular Dependency - Solutions?

```java
// Problem: Circular dependency
@Component
class A {
    @Autowired
    private B b;
}

@Component
class B {
    @Autowired
    private A a;  // Circular!
}

// Solution 1: Use @Lazy
@Component
class A {
    @Autowired
    @Lazy
    private B b;  // Lazy initialization breaks cycle
}

// Solution 2: Constructor injection với @Lazy
@Component
class A {
    private B b;
    
    public A(@Lazy B b) {
        this.b = b;
    }
}

// Solution 3: Refactor - extract common logic
@Component
class CommonService {
    // Shared logic
}

@Component
class A {
    @Autowired
    private CommonService commonService;
}

@Component
class B {
    @Autowired
    private CommonService commonService;
}
```

### Q4: Spring Boot Auto-configuration - How it works?

```java
// Spring Boot uses @Conditional annotations
@Configuration
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(name = "spring.datasource.url")
public class DataSourceAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        // Auto-configure DataSource
        return DataSourceBuilder.create().build();
    }
}

// Custom auto-configuration
@Configuration
@ConditionalOnClass(MyService.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class MyServiceAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public MyService myService() {
        return new MyService();
    }
}

// META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.MyServiceAutoConfiguration
```

### Q5: Spring Boot Profiles - Best Practices?

```java
// application-dev.properties
spring.datasource.url=jdbc:h2:mem:testdb
logging.level.root=DEBUG

// application-prod.properties
spring.datasource.url=jdbc:mysql://prod-server:3306/mydb
logging.level.root=INFO

// application.yml với profiles
spring:
  profiles:
    active: dev
  datasource:
    url: ${DB_URL:jdbc:h2:mem:testdb}

---
spring:
  profiles: prod
  datasource:
    url: ${DB_URL}
```

### Q6: Spring Security - Basic Configuration?

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin()
            .and()
            .httpBasic();
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
```

## Tổng kết

- **IoC/DI**: Inversion of Control, Dependency Injection
- **Bean Scopes**: Singleton, Prototype, Request, Session
- **Annotations**: @Component, @Service, @Repository, @Controller, @Autowired
- **AOP**: Cross-cutting concerns, aspects, advices
- **Spring MVC**: Controllers, request mapping
- **Spring Boot**: Auto-configuration, starters, embedded server
- **Advanced**: Bean lifecycle, conditional beans, events, SpEL, transactions
- **Best Practices**: Constructor injection, proper scopes, avoid circular dependencies
