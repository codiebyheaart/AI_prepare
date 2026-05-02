# Spring Boot — Interview Preparation Guide

> **Target:** Senior Associate (8+ yrs) — Java Spring Boot Developer

---

## 1. Spring Boot Architecture & Auto-Configuration

Spring Boot eliminates boilerplate by auto-configuring beans based on classpath dependencies.

```java
@SpringBootApplication // = @Configuration + @EnableAutoConfiguration + @ComponentScan
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}
```

**How Auto-Configuration works:**
- Scans `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Uses `@Conditional` annotations (`@ConditionalOnClass`, `@ConditionalOnMissingBean`)
- You can override any auto-config by defining your own bean

```java
// Custom override example
@Configuration
public class CustomDataSourceConfig {
    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://localhost:5432/mydb")
            .username("admin")
            .build();
    }
}
```

---

## 2. Dependency Injection & IoC Container

```java
// Constructor Injection (PREFERRED)
@Service
public class OrderService {
    private final PaymentGateway paymentGateway;
    private final InventoryService inventoryService;

    public OrderService(PaymentGateway paymentGateway, InventoryService inventoryService) {
        this.paymentGateway = paymentGateway;
        this.inventoryService = inventoryService;
    }
}

// Bean Scopes
@Component
@Scope("prototype") // new instance each time (default is "singleton")
public class ReportGenerator { }
```

**Key Annotations:**
| Annotation | Purpose |
|---|---|
| `@Component` | Generic Spring-managed bean |
| `@Service` | Business logic layer |
| `@Repository` | Data access layer (adds exception translation) |
| `@Controller` | Web MVC controller |
| `@Configuration` | Java-based configuration class |
| `@Bean` | Method-level bean definition |
| `@Qualifier` | Resolve ambiguity when multiple beans of same type |

---

## 3. REST API Design

```java
@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<OrderDTO> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderDTO created = orderService.create(request);
        URI location = URI.create("/api/v1/orders/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> update(@PathVariable Long id,
                                            @Valid @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Validation:**
```java
public class CreateOrderRequest {
    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotEmpty(message = "At least one item required")
    private List<@Valid OrderItemRequest> items;

    @Email(message = "Invalid email")
    private String email;

    @Min(value = 1) @Max(value = 100)
    private int quantity;
}
```

---

## 4. Global Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));

        ErrorResponse error = new ErrorResponse(400, "Validation failed", LocalDateTime.now());
        error.setFieldErrors(errors);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse error = new ErrorResponse(500, "Internal server error", LocalDateTime.now());
        return ResponseEntity.status(500).body(error);
    }
}

// Custom exception
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id);
    }
}
```

---

## 5. Spring Data JPA & Hibernate

```java
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Helper method for bidirectional relationship
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}

// Repository with custom queries
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.customerName LIKE %:name% AND o.status = :status")
    Page<Order> searchByNameAndStatus(@Param("name") String name,
                                       @Param("status") OrderStatus status,
                                       Pageable pageable);

    @Query(value = "SELECT * FROM orders WHERE created_at > :since", nativeQuery = true)
    List<Order> findRecentOrders(@Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") OrderStatus status);
}
```

**N+1 Problem Fix:**
```java
// BAD — triggers N+1 queries
@OneToMany(mappedBy = "order", fetch = FetchType.EAGER)

// GOOD — use JOIN FETCH when needed
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
Optional<Order> findByIdWithItems(@Param("id") Long id);

// ALSO GOOD — EntityGraph
@EntityGraph(attributePaths = {"items", "items.product"})
Optional<Order> findById(Long id);
```

---

## 6. Spring Security (JWT + OAuth2)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// JWT Filter
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        String username = jwtService.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails user = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(token, user)) {
                var authToken = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(request, response);
    }
}
```

---

## 7. Caching

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeValuesWith(SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer()));
        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(config).build();
    }
}

@Service
public class ProductService {

    @Cacheable(value = "products", key = "#id")
    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @CachePut(value = "products", key = "#product.id")
    public Product update(Product product) {
        return productRepository.save(product);
    }

    @CacheEvict(value = "products", key = "#id")
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Scheduled(fixedRate = 3600000) // evict every hour
    public void evictAllCache() { }
}
```

---

## 8. Async Processing

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class NotificationService {

    @Async("taskExecutor")
    public CompletableFuture<String> sendEmailAsync(String to, String body) {
        // simulate long operation
        emailClient.send(to, body);
        return CompletableFuture.completedFuture("Email sent to " + to);
    }
}

// Usage — parallel calls
CompletableFuture<String> email = notificationService.sendEmailAsync(user.getEmail(), body);
CompletableFuture<String> sms = notificationService.sendSmsAsync(user.getPhone(), body);
CompletableFuture.allOf(email, sms).join(); // wait for both
```

---

## 9. Profiles & Configuration

```yaml
# application.yml
spring:
  profiles:
    active: dev

---
# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:devdb
  jpa:
    show-sql: true

---
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/proddb
    username: ${DB_USER}
    password: ${DB_PASS}
  jpa:
    show-sql: false
```

```java
@Configuration
@Profile("prod")
public class ProdSecurityConfig {
    // Production-specific security settings
}

// Custom config properties
@ConfigurationProperties(prefix = "app.trading")
@Validated
public class TradingProperties {
    @NotBlank private String apiKey;
    @Min(1) private int maxRetries;
    @DurationUnit(ChronoUnit.SECONDS) private Duration timeout;
    // getters, setters
}
```

---

## 10. Testing

```java
// Unit test with Mockito
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private PaymentGateway paymentGateway;
    @InjectMocks private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        CreateOrderRequest request = new CreateOrderRequest("John", List.of(item));
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(paymentGateway.charge(any())).thenReturn(PaymentResult.success());

        OrderDTO result = orderService.create(request);

        assertThat(result.getCustomerName()).isEqualTo("John");
        verify(paymentGateway).charge(any());
        verify(orderRepository).save(any());
    }
}

// Integration test
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldCreateAndRetrieveOrder() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest("Jane", List.of(item));

        String response = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerName").value("Jane"))
            .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/v1/orders/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerName").value("Jane"));
    }
}

// Test Slices
@DataJpaTest           // Only JPA components
@WebMvcTest            // Only web layer
@JsonTest              // Only JSON serialization
```

---

## 11. Microservices Patterns

### Service Discovery (Eureka)
```java
// Eureka Server
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServer { }

// Eureka Client (in each microservice)
@SpringBootApplication
@EnableDiscoveryClient
public class OrderService { }
```

### API Gateway (Spring Cloud Gateway)
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
            - name: CircuitBreaker
              args:
                name: orderCB
                fallbackUri: forward:/fallback/orders
```

### Circuit Breaker (Resilience4j)
```java
@Service
public class PaymentService {

    @CircuitBreaker(name = "payment", fallbackMethod = "paymentFallback")
    @Retry(name = "payment", fallbackMethod = "paymentFallback")
    @RateLimiter(name = "payment")
    public PaymentResponse processPayment(PaymentRequest req) {
        return paymentClient.charge(req);
    }

    public PaymentResponse paymentFallback(PaymentRequest req, Throwable t) {
        return PaymentResponse.pending("Payment queued for retry");
    }
}
```

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      payment:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
  retry:
    instances:
      payment:
        maxAttempts: 3
        waitDuration: 2s
```

---

## 12. Messaging (Kafka)

```java
// Producer
@Service
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publishOrderCreated(Order order) {
        OrderEvent event = new OrderEvent("ORDER_CREATED", order.getId(), order);
        kafkaTemplate.send("order-events", order.getId().toString(), event);
    }
}

// Consumer
@Service
public class OrderEventConsumer {

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void handleOrderEvent(OrderEvent event) {
        switch (event.getType()) {
            case "ORDER_CREATED" -> inventoryService.reserve(event.getOrder().getItems());
            case "ORDER_CANCELLED" -> inventoryService.release(event.getOrder().getItems());
        }
    }
}

// Configuration
@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name("order-events")
            .partitions(3)
            .replicas(1)
            .build();
    }
}
```

---

## 13. Database Migration (Flyway)

```sql
-- V1__create_orders_table.sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V2__add_email_column.sql
ALTER TABLE orders ADD COLUMN email VARCHAR(255);

-- V3__create_order_items_table.sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL
);
```

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

---

## 14. Actuator & Monitoring

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
  health:
    circuitbreakers:
      enabled: true
```

```java
// Custom health indicator
@Component
public class ExternalApiHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        boolean isUp = externalApiClient.ping();
        return isUp ? Health.up().withDetail("api", "reachable").build()
                     : Health.down().withDetail("api", "unreachable").build();
    }
}

// Custom metric
@Service
public class OrderService {
    private final Counter orderCounter;

    public OrderService(MeterRegistry registry) {
        this.orderCounter = Counter.builder("orders.created")
            .description("Total orders created")
            .tag("type", "all")
            .register(registry);
    }

    public Order create(CreateOrderRequest req) {
        Order order = // ... save logic
        orderCounter.increment();
        return order;
    }
}
```

---

## 15. Swagger / OpenAPI 3

```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Order Management API")
                .version("1.0")
                .description("REST API for managing orders"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}

// On controller methods
@Operation(summary = "Create a new order", description = "Creates order and triggers payment")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Order created"),
    @ApiResponse(responseCode = "400", description = "Invalid request")
})
@PostMapping
public ResponseEntity<OrderDTO> create(@Valid @RequestBody CreateOrderRequest request) { }
```

---

## Quick Reference — Common Interview Questions

| Question | Key Answer |
|----------|-----------|
| `@SpringBootApplication` does what? | Combines `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` |
| Difference `@Component` vs `@Bean`? | `@Component` = class-level auto-detected; `@Bean` = method-level in `@Configuration` |
| How to handle N+1? | `JOIN FETCH`, `@EntityGraph`, or batch fetching |
| `@Transactional` propagation default? | `REQUIRED` — joins existing or creates new |
| How does `@Async` work? | Proxied via AOP; must be called from different bean |
| Circuit breaker states? | CLOSED → OPEN → HALF_OPEN |
| `@Cacheable` vs `@CachePut`? | `@Cacheable` skips method if cached; `@CachePut` always executes |
| How to externalize secrets? | Vault, env vars, AWS Secrets Manager, `@ConfigurationProperties` |

---

*Next: `springboot-ai.md` →*
