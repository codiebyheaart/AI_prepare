# 🌱 Spring Boot — Core Concepts in Simple Words

> **For:** 8+ Year Senior Associate preparing for Java/Spring Boot interviews (2026–2027)

---

## 1. What is Spring Boot?

Spring Boot is like a **pre-built kitchen** — everything is already set up. You just cook (write business logic). You don't have to configure the stove, oven, or refrigerator manually.

- Built on top of the Spring Framework
- Removes boilerplate XML configuration
- Provides **auto-configuration**, embedded servers, and production-ready features out of the box

---

## 2. Spring vs Spring Boot

| Spring | Spring Boot |
|--------|-------------|
| You set up everything manually | Auto-configures most things |
| XML or Java config needed | Minimal config needed |
| No embedded server | Embedded Tomcat/Jetty/Undertow |
| Complex project setup | Start in minutes with Spring Initializr |

---

## 3. Spring Boot Key Annotations (Plain English)

### `@SpringBootApplication`
The **main door** of your app. It combines 3 annotations:
- `@Configuration` → This class has config/beans
- `@EnableAutoConfiguration` → Let Spring Boot auto-set things
- `@ComponentScan` → Scan this package for Spring components

### `@RestController`
A shortcut that says: "This class handles HTTP requests and always returns data (JSON/XML), not a web page."

### `@RequestMapping` / `@GetMapping` / `@PostMapping`
Map a URL to a method. Like telling a postal address which house to deliver to.

### `@Service`
Marks a class as a **business logic handler**. Spring manages it automatically.

### `@Repository`
Marks a class that **talks to the database**. Spring adds extra features like exception translation.

### `@Component`
A generic marker — "Hey Spring, manage this class for me."

### `@Autowired`
Asks Spring: "Please inject the right object here, I don't want to create it manually."

### `@Value`
Reads a value from your `application.properties` or environment variables.

### `@Bean`
You're telling Spring: "When I call this method, treat its return value as a managed object."

### `@Transactional`
"Everything in this method should happen together — if one step fails, roll back everything."

---

## 4. Dependency Injection (DI) — Simple Explanation

**Without DI:** You go to the store and buy milk yourself.

**With DI:** You tell someone "I need milk" and they deliver it to you. You don't care where it came from.

Spring is the **delivery person**. You declare what you need, Spring provides it.

### Types of Injection
- **Constructor Injection** → Preferred. Pass dependencies via constructor. Best for mandatory deps.
- **Setter Injection** → Pass via setter method. Good for optional deps.
- **Field Injection** (`@Autowired` on field) → Convenient but hard to test. Avoid in production.

---

## 5. Spring Boot Application Layers

```
[Client/Browser/API Consumer]
        ↓
[Controller Layer]     → Handles HTTP requests/responses
        ↓
[Service Layer]        → Business logic lives here
        ↓
[Repository Layer]     → Talks to the database
        ↓
[Database]
```

Each layer has **one job** — this makes the app easy to maintain and test.

---

## 6. application.properties vs application.yml

Both are configuration files. `yml` is just more readable:

**application.properties:**
```
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
```

**application.yml:**
```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
```

---

## 7. Spring Boot Profiles

Profiles let you have **different settings** for different environments (dev, test, prod).

- `application-dev.properties` → Used when profile = `dev`
- `application-prod.properties` → Used when profile = `prod`
- Activate with: `spring.profiles.active=dev`

Think of it as **wardrobe choices** — you dress differently for office vs. gym.

---

## 8. Auto-Configuration

Spring Boot looks at what jars are on the classpath and **guesses** what you need.

- Have MySQL driver? → Configure a DataSource automatically.
- Have Spring Security? → Protect all endpoints automatically.
- Have Spring Web? → Set up a DispatcherServlet automatically.

You can override any auto-config with your own settings.

---

## 9. Spring Boot Starter Dependencies

Starters are **pre-packaged bundles** of related dependencies.

| Starter | What it includes |
|---------|-----------------|
| `spring-boot-starter-web` | Spring MVC, Tomcat, Jackson |
| `spring-boot-starter-data-jpa` | Hibernate, Spring Data JPA |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-test` | JUnit, Mockito, AssertJ |
| `spring-boot-starter-actuator` | Monitoring endpoints |
| `spring-boot-starter-cache` | Caching support |

---

## 10. Spring Data JPA — Simple Explanation

JPA (Java Persistence API) is a way to **store and retrieve Java objects from a database** without writing SQL manually.

- You create an **Entity** (a Java class mapped to a DB table)
- You create a **Repository** (an interface that handles DB operations)
- Spring generates the SQL queries for you!

**Common Repository Methods (auto-generated):**
- `findAll()` → Get everything
- `findById(id)` → Get by ID
- `save(entity)` → Insert or update
- `delete(entity)` → Remove
- `findByName(name)` → Custom query (Spring figures out the SQL!)

---

## 11. Spring Security in Simple Words

Spring Security is a **bouncer at the club door**:
- **Authentication** → "Who are you?" (login, JWT, OAuth)
- **Authorization** → "Are you allowed in here?" (roles, permissions)

### Key Concepts
- **JWT (JSON Web Token):** A token given after login. User sends it with every request. Stateless.
- **OAuth2:** Login with Google/GitHub etc. — delegating authentication to another service.
- **CSRF Protection:** Prevents malicious forms from submitting on behalf of a logged-in user.
- **BCrypt:** A password hashing algorithm. Never store plain text passwords.

---

## 12. Spring Boot Actuator

Actuator gives you **health monitoring endpoints** for free.

| Endpoint | What it shows |
|----------|--------------|
| `/actuator/health` | Is the app healthy? |
| `/actuator/metrics` | CPU, memory, request count |
| `/actuator/info` | App version, build info |
| `/actuator/env` | Current environment properties |
| `/actuator/beans` | All Spring beans |

Think of it as a **car dashboard** — you can see engine temperature, fuel level, speed.

---

## 13. Exception Handling

Use `@ControllerAdvice` + `@ExceptionHandler` to handle errors **globally** in one place instead of in every controller.

Think of it as a **centralized complaint desk** — any problem from anywhere in the app gets routed here.

---

## 14. Caching in Spring Boot

Caching stores results of expensive operations so you don't have to repeat them.

- `@EnableCaching` → Turn on caching
- `@Cacheable` → "Cache the result of this method"
- `@CacheEvict` → "Remove this from cache when something changes"

Like **saving a cooked meal in the fridge** — next time someone's hungry, just reheat instead of cooking again.

---

## 15. Spring Boot Testing

| Type | What it does | Annotation |
|------|-------------|-----------|
| Unit Test | Test a single class in isolation | `@ExtendWith(MockitoExtension.class)` |
| Integration Test | Test multiple layers together | `@SpringBootTest` |
| Web Layer Test | Test only controllers | `@WebMvcTest` |
| Data Layer Test | Test only repository/DB | `@DataJpaTest` |

**Mockito** is used to create fake (mock) objects so you can test a class without real dependencies.

---

## 16. Bean Scopes

| Scope | Meaning |
|-------|---------|
| `singleton` | One instance for the whole app (default) |
| `prototype` | New instance every time you request it |
| `request` | One instance per HTTP request |
| `session` | One instance per user session |

---

## 17. Spring Boot Event-Driven Concepts

- **ApplicationEvent:** Custom events you can publish within the app
- **@EventListener:** Listens to events and reacts
- Like a **newspaper subscription** — publish news once, all subscribers get it

---

## 18. RestTemplate vs WebClient

| RestTemplate | WebClient |
|---|---|
| Old way to call REST APIs | New way (reactive) |
| Blocking (waits for response) | Non-blocking (continues while waiting) |
| Simple to use | Better for high performance |

**Rule of thumb:** Use WebClient in new projects.

---

## 19. Circuit Breaker (Resilience4j)

If a downstream service fails repeatedly, a **circuit breaker** stops calling it and returns a fallback response.

Like a **trip switch in your home electrical box** — if there's too much load, it trips to protect the system.

---

## 20. Spring Boot + Microservices Key Concepts

| Concept | What it means |
|---------|--------------|
| API Gateway | Single entry point for all microservices |
| Service Discovery | Services find each other (Eureka) |
| Config Server | Centralized configuration for all services |
| Load Balancing | Distribute traffic across service instances |
| Feign Client | Declarative REST client between services |
| Message Queue | Async communication (Kafka, RabbitMQ) |

---

## 21. Key Interview Points to Remember

- **Why Spring Boot over Spring?** → Less config, faster start, embedded server, auto-config
- **What is IoC (Inversion of Control)?** → Spring controls object creation and lifecycle, not you
- **What is AOP (Aspect-Oriented Programming)?** → Cross-cutting concerns (logging, security) separated from business logic
- **Difference between `@Component`, `@Service`, `@Repository`?** → All register beans; `@Repository` adds DB exception translation; `@Service` is semantic
- **What is `@Transactional`?** → Ensures database operations are atomic — all succeed or all fail
- **Lazy vs Eager loading in JPA?** → Lazy = load related data only when accessed; Eager = load everything immediately
- **N+1 Problem?** → Fetching 1 parent + N children with N separate SQL queries. Fix with JOIN FETCH or batch fetching.

---

## Summary

Spring Boot makes Java development fast, clean, and production-ready. The core pillars are:
1. **Auto-configuration** — Less manual setup
2. **Dependency Injection** — Loose coupling
3. **Layered Architecture** — Separation of concerns
4. **Starter Dependencies** — Quick bootstrapping
5. **Actuator & Testing** — Built-in quality & monitoring

> 💡 **Interview Tip:** Always explain *why* a feature exists, not just *what* it does. Interviewers love candidates who understand the "why."
