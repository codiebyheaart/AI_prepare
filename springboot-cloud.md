# ☁️ Spring Boot + Cloud — Deployment & Monitoring Guide

> **For:** 8+ Year Senior Associate preparing for Java/Spring Boot/Cloud interviews (2026–2027)

---

## 1. Why Cloud Matters for Spring Boot Developers?

Modern applications don't live on a single server anymore. They live in the **cloud** — a collection of managed services that handle compute, storage, networking, and more.

As a Senior Java/Spring Boot developer, you need to know:
- How to **package** your Spring Boot app for the cloud
- How to **deploy** it (manually or automatically via CI/CD)
- How to **monitor** it in production
- How to **scale** it when load increases

---

## 2. The Cloud Deployment Landscape

```
[Your Code (GitHub/GitLab)]
          ↓
[CI/CD Pipeline (GitHub Actions / Jenkins / GitLab CI)]
          ↓
[Build: Maven/Gradle → JAR or Docker Image]
          ↓
[Push to Container Registry (ECR / Docker Hub / GCR)]
          ↓
[Deploy to Cloud]
    ├── AWS (ECS, EKS, Elastic Beanstalk, Lambda)
    ├── GCP (Cloud Run, GKE, App Engine)
    └── Azure (AKS, App Service, Functions)
          ↓
[Production Running App]
          ↓
[Monitoring (CloudWatch / Prometheus / Grafana / Datadog)]
```

---

## 3. Packaging a Spring Boot App for the Cloud

### Step 1 — Build a Fat JAR
Maven/Gradle packages everything (code + dependencies) into one runnable `.jar` file.

```
mvn clean package   →   target/myapp-1.0.jar
java -jar myapp.jar →   App starts on port 8080
```

### Step 2 — Dockerize the App
Docker packages the app **with its runtime environment** (Java JDK, configs, etc.) into a **container image**.

A `Dockerfile` describes how to build the image:

```
[Base Image: Eclipse Temurin 17 JDK]
          ↓
[Copy JAR file into image]
          ↓
[Set startup command: java -jar app.jar]
          ↓
[Build: docker build -t myapp:1.0 .]
          ↓
[Run: docker run -p 8080:8080 myapp:1.0]
```

### Step 3 — Push to Container Registry
```
Docker Hub       → docker push myusername/myapp:1.0
AWS ECR          → Push to Amazon's private registry
Google GCR       → Push to Google's registry
Azure ACR        → Push to Azure Container Registry
```

---

## 4. Cloud Providers Overview

### AWS (Amazon Web Services) — Most Popular

| Service | What it is | Use for Spring Boot |
|---------|------------|---------------------|
| **EC2** | Virtual machines | Run JAR directly on VM |
| **ECS** | Container service (no K8s) | Deploy Docker containers |
| **EKS** | Kubernetes on AWS | Orchestrate multiple microservices |
| **Elastic Beanstalk** | PaaS (auto-manages infra) | Quick deploy, minimal config |
| **Lambda** | Serverless functions | Spring Boot on Lambda (GraalVM native) |
| **RDS** | Managed databases | PostgreSQL, MySQL for your app |
| **ElastiCache** | Managed Redis | Caching layer |
| **S3** | Object storage | Store files, documents, ML models |
| **CloudWatch** | Monitoring & logs | Application monitoring |
| **API Gateway** | Managed API router | Route public APIs to Lambda/ECS |

### GCP (Google Cloud Platform)

| Service | Spring Boot Use |
|---------|----------------|
| **Cloud Run** | Deploy Docker containers serverlessly |
| **GKE** | Kubernetes for microservices |
| **App Engine** | PaaS for Spring Boot apps |
| **Cloud SQL** | Managed PostgreSQL/MySQL |
| **Pub/Sub** | Messaging (like Kafka) |
| **Vertex AI** | Managed AI/ML + Gemini APIs |
| **Cloud Monitoring** | Metrics, logs, traces |

### Azure

| Service | Spring Boot Use |
|---------|----------------|
| **App Service** | Deploy Spring Boot directly |
| **AKS** | Kubernetes |
| **Azure Spring Apps** | Managed Spring Boot platform |
| **Cosmos DB** | Distributed NoSQL |
| **Application Insights** | APM monitoring |

---

## 5. Deploying to AWS Elastic Beanstalk (Simplest Path)

**Elastic Beanstalk** is like telling AWS: "Here's my JAR file. You handle the rest."

### What AWS Beanstalk Does Automatically
- Provisions EC2 instances
- Configures load balancer
- Sets up auto-scaling
- Manages deployments and rollbacks
- Provides health monitoring

### Deployment Flow
```
[Spring Boot JAR] 
        ↓
[Elastic Beanstalk Console or EB CLI]
        ↓
[AWS creates: EC2 + Load Balancer + Auto Scaling Group]
        ↓
[App accessible via: myapp.elasticbeanstalk.com]
```

### Key Configuration (application.properties for cloud)
```
# Use environment variables (injected by AWS/Kubernetes)
server.port=${PORT:8080}
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
```

> **Never hardcode secrets in your code or config files!**

---

## 6. Deploying to AWS ECS (Containers — Intermediate Level)

**ECS (Elastic Container Service)** runs your Docker containers.

### Key Concepts

| Term | Meaning |
|------|---------|
| **Task Definition** | Blueprint of your container (image, CPU, memory, ports) |
| **Task** | A running instance of a Task Definition |
| **Service** | Keeps N tasks always running; handles restarts |
| **Cluster** | Group of EC2 instances or Fargate capacity |
| **Fargate** | Serverless ECS — no EC2 to manage |

### ECS Deployment Flow
```
[Docker Image] → [Push to ECR]
                        ↓
               [Create Task Definition]
               (image URI, port 8080, env vars, memory)
                        ↓
               [Create ECS Service]
               (desired count=2, load balancer, auto-restart)
                        ↓
               [Load Balancer routes traffic]
               [2 containers running your Spring Boot app]
```

---

## 7. Kubernetes (K8s) — Industry Standard for Microservices

Kubernetes orchestrates many containers across many machines. Think of it as the **operating system for your cloud infrastructure**.

### Core K8s Concepts

| Object | Plain English |
|--------|--------------|
| **Pod** | Smallest unit — wraps your container |
| **Deployment** | Manages replicas of your pod (keep 3 running) |
| **Service** | Stable network endpoint to reach your pods |
| **Ingress** | HTTP router — routes external traffic to services |
| **ConfigMap** | Non-secret configuration (env vars, properties) |
| **Secret** | Sensitive config (passwords, API keys) |
| **Namespace** | Logical grouping (dev, staging, prod) |
| **HPA** | Horizontal Pod Autoscaler — auto scale on CPU/memory |

### How a Spring Boot App Lives in K8s
```
[Internet Traffic]
        ↓
[Ingress Controller] (NGINX / AWS ALB)
        ↓
[Kubernetes Service] (load balances between pods)
        ↓
[Pod 1: Spring Boot Container]
[Pod 2: Spring Boot Container]  ← Kubernetes keeps these running
[Pod 3: Spring Boot Container]
        ↓
[Kubernetes Secret → DB password injected as env var]
[ConfigMap → application.yml values injected]
```

---

## 8. CI/CD Pipeline — Automated Deployment

CI/CD = **Continuous Integration / Continuous Deployment**

Every code push automatically builds, tests, and deploys your app.

### GitHub Actions Pipeline Flow
```
[Developer pushes code to GitHub]
               ↓
[GitHub Actions triggers workflow]
               ↓
[Step 1: Run unit tests (Maven test)]
               ↓
[Step 2: Build JAR (Maven package)]
               ↓
[Step 3: Build Docker image]
               ↓
[Step 4: Push image to ECR/Docker Hub]
               ↓
[Step 5: Deploy to ECS/K8s]
               ↓
[Slack notification: Deploy successful ✅]
```

### Popular CI/CD Tools
| Tool | When to Use |
|------|------------|
| **GitHub Actions** | GitHub-hosted projects |
| **Jenkins** | Self-hosted, highly customizable |
| **GitLab CI** | GitLab-hosted projects |
| **AWS CodePipeline** | All-AWS stack |
| **ArgoCD** | GitOps for Kubernetes |
| **Tekton** | Cloud-native K8s pipelines |

---

## 9. Environment Variables & Secrets Management

### Rule #1: Never Hardcode Secrets

```
❌ spring.datasource.password=myPassword123
✅ spring.datasource.password=${DB_PASSWORD}
```

### Where Secrets Come From

| Method | How |
|--------|-----|
| **AWS Secrets Manager** | Fetch at runtime; auto-rotate |
| **AWS Parameter Store** | Store configs, fetch on startup |
| **K8s Secrets** | Injected as env vars into pods |
| **HashiCorp Vault** | Enterprise-grade secrets management |
| **`.env` files** | Local development only, never commit! |

### Spring Boot Cloud Config Server
Centralize configuration for all microservices in one place (Spring Cloud Config):

```
[Config Server] ← reads from Git repo with all properties files
       ↓
[Service A] [Service B] [Service C]
   all fetch their config from Config Server on startup
```

---

## 10. Spring Boot Cloud-Ready Features

### 10.1 Health Checks (Kubernetes Probes)
Kubernetes needs to know if your pod is alive and ready to serve traffic.

```yaml
# Kubernetes deployment probes — Spring Actuator makes this easy
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 20
```

- **Liveness Probe:** Is the app alive? (Restart if fails)
- **Readiness Probe:** Is the app ready to receive traffic?

### 10.2 Graceful Shutdown
```yaml
# application.yml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```
App finishes in-flight requests before shutting down — no dropped requests during deployments.

### 10.3 Externalized Configuration
Spring Boot's `@ConfigurationProperties` + environment variables = cloud-native config.

### 10.4 Spring Cloud Netflix Stack

| Component | Purpose |
|-----------|---------|
| **Eureka** | Service discovery — services register and find each other |
| **Zuul/Gateway** | API Gateway — single entry point |
| **Ribbon** | Client-side load balancing |
| **Hystrix/Resilience4j** | Circuit breaker — handle service failures |
| **Config Server** | Centralized configuration |

---

## 11. Monitoring in Production

### The 3 Pillars of Observability

```
Observability
├── Metrics  → Numbers over time (CPU, request rate, error rate)
├── Logs     → Text records of what happened
└── Traces   → End-to-end request journey across services
```

### 11.1 Spring Boot Actuator + Micrometer
The foundation of Spring Boot observability:

| Actuator Endpoint | What it exposes |
|------------------|----------------|
| `/actuator/health` | App + DB + dependencies health |
| `/actuator/metrics` | JVM memory, HTTP requests, DB pool |
| `/actuator/prometheus` | Metrics in Prometheus format |
| `/actuator/loggers` | View/change log levels at runtime |
| `/actuator/threaddump` | Thread state — detect deadlocks |
| `/actuator/heapdump` | JVM heap dump for memory analysis |

### 11.2 Prometheus + Grafana Stack
The most popular open-source monitoring stack:

```
[Spring Boot App]
       ↓
[Actuator /actuator/prometheus endpoint]
       ↓
[Prometheus] ← scrapes metrics every 15 seconds
       ↓
[Grafana] ← queries Prometheus, shows beautiful dashboards
```

**What to monitor in Grafana:**
- HTTP request rate, latency, error rate
- JVM heap usage, GC pause time
- DB connection pool usage
- Custom business metrics (trades executed, loans processed)

### 11.3 Centralized Logging

```
[Spring Boot Logs (JSON format)]
       ↓
[Log Aggregator: Fluentd / Logstash / CloudWatch Agent]
       ↓
[Log Store: Elasticsearch / CloudWatch Logs / Loki]
       ↓
[Visualization: Kibana / CloudWatch Dashboards / Grafana]
```

**Best Practices:**
- Use **structured JSON logs** (not plain text) in production
- Include `traceId`, `userId`, `requestId` in every log
- Never log sensitive data (passwords, PII, card numbers)
- Log at appropriate levels: ERROR for failures, INFO for key events, DEBUG for development

### 11.4 Distributed Tracing
When a request flows through multiple microservices, tracing shows the full journey.

```
[User Request: GET /api/loan/apply]
       │
       ├── [Loan Service: 45ms]
       │         ├── [Document Service: 20ms]
       │         └── [Credit Score Service: 18ms]
       └── [Notification Service: 5ms]

Total: 88ms — identify the bottleneck!
```

Tools:
- **Zipkin** — Open-source distributed tracing
- **Jaeger** — CNCF distributed tracing
- **AWS X-Ray** — AWS native tracing
- **OpenTelemetry** — Vendor-neutral standard for traces + metrics + logs

### 11.5 Alerting
Set up alerts so you're notified before users notice problems:

| Alert | Condition |
|-------|-----------|
| High Error Rate | HTTP 5xx > 1% of requests |
| Slow Response | p99 latency > 2 seconds |
| Service Down | Health check fails for 3 minutes |
| Memory Pressure | JVM heap > 85% for 10 minutes |
| DB Pool Exhausted | Active connections = max pool size |

Alerting tools: **PagerDuty, OpsGenie, AWS SNS, Slack notifications**

---

## 12. Auto-Scaling

### Horizontal vs Vertical Scaling

| Type | What it means | When to use |
|------|--------------|-------------|
| **Horizontal (Scale Out)** | Add more pods/instances | Stateless apps — most Spring Boot apps |
| **Vertical (Scale Up)** | Give more CPU/memory | Stateful, memory-intensive workloads |

### Kubernetes HPA (Horizontal Pod Autoscaler)
```
Normal load: 2 pods running
Traffic spike detected: CPU > 70%
HPA scales to: 5 pods automatically
Traffic decreases: HPA scales back to 2 pods
```

### AWS Auto Scaling Group
Same concept for EC2 instances — automatically adds/removes VMs based on CloudWatch metrics.

---

## 13. Cloud Database Best Practices

### Managed Databases vs Self-Hosted

| Managed (RDS, Cloud SQL) | Self-Hosted (DB on EC2) |
|--------------------------|------------------------|
| Automated backups | You manage backups |
| Automatic failover | You handle failover |
| Patching managed | You patch manually |
| Higher cost | Lower cost |
| Less control | Full control |

**For most Spring Boot apps → Use managed databases (RDS, Cloud SQL)**

### Connection Pooling
In cloud environments, DB connections are precious:
- Use **HikariCP** (Spring Boot default) — extremely fast pool
- Configure pool size based on your workload
- Monitor pool metrics via Actuator

---

## 14. Cloud Cost Optimization

### Common Cost Drains
- Oversized EC2 instances
- Idle RDS instances running 24/7
- Storing too many old Docker images in ECR
- Excessive CloudWatch log retention

### Key Strategies
- **Right-size** instances — use metrics to choose correct instance type
- **Spot Instances** (AWS) — 70% cheaper for fault-tolerant workloads
- **Reserved Instances** — commit 1-3 years for 40-60% savings
- **Auto-scaling** — don't run 10 pods at midnight when traffic is low
- **Database scheduling** — stop dev/staging DBs at night

---

## 15. Cloud-Native 12-Factor App Principles

The 12-Factor App is the **bible for cloud-native applications**:

| Factor | Principle | Spring Boot Application |
|--------|-----------|------------------------|
| 1. Codebase | One codebase, tracked in Git | Single Git repo |
| 2. Dependencies | Explicit dependency declaration | Maven/Gradle `pom.xml` |
| 3. Config | Config in environment, not code | `${ENV_VAR}` in properties |
| 4. Backing Services | Treat as attached resources | DB URL via env var |
| 5. Build/Release/Run | Separate build and run stages | Maven build → Docker image → Deploy |
| 6. Processes | Stateless processes | No session stored in-memory |
| 7. Port Binding | Export services via port | `server.port=8080` |
| 8. Concurrency | Scale via process model | Horizontal pod scaling |
| 9. Disposability | Fast startup, graceful shutdown | Spring Boot graceful shutdown |
| 10. Dev/Prod Parity | Keep dev/staging/prod similar | Docker ensures consistency |
| 11. Logs | Treat as event streams | JSON logs to stdout |
| 12. Admin Processes | Run admin tasks as one-off processes | Spring Boot CommandLineRunner |

---

## 16. Spring Boot on AWS Lambda (Serverless)

For low-traffic APIs or scheduled tasks, run Spring Boot **serverlessly**:

```
[API Request]
      ↓
[API Gateway]
      ↓
[Lambda (Spring Boot cold start → handle request → shutdown)]
      ↓
[Response]
```

**Pros:** Zero idle cost, auto-scales to zero  
**Cons:** Cold start latency (use GraalVM Native Image to reduce)

**GraalVM Native Image:** Compiles Spring Boot to a native binary — starts in ~50ms vs 3-5 seconds for JVM startup.

---

## 17. Interview Key Points

- **"How do you make a Spring Boot app cloud-ready?"**
  → Externalize config via env vars, use Actuator health probes, enable graceful shutdown, containerize with Docker, implement structured logging

- **"What's the difference between ECS and EKS?"**
  → ECS is AWS-proprietary container service, simpler to set up. EKS is managed Kubernetes, more control and portable across clouds

- **"How do you monitor Spring Boot in production?"**
  → Actuator exposes Prometheus metrics, Grafana for dashboards, ELK stack for logs, Jaeger for distributed tracing, alert on SLOs

- **"What is a circuit breaker and why is it needed in cloud?"**
  → Prevents cascade failures — if Service B is slow/down, circuit breaker returns fallback immediately instead of waiting and exhausting Service A's threads

- **"How do you handle secrets in cloud deployments?"**
  → AWS Secrets Manager or Parameter Store, never commit to Git, inject as environment variables into containers

---

## Summary

| Layer | Tools |
|-------|-------|
| **Packaging** | Maven/Gradle → JAR → Docker Image |
| **Registry** | ECR, Docker Hub, GCR |
| **Deployment** | ECS, EKS, Cloud Run, Elastic Beanstalk |
| **CI/CD** | GitHub Actions, Jenkins, ArgoCD |
| **Config/Secrets** | AWS Secrets Manager, K8s Secrets, Spring Cloud Config |
| **Metrics** | Actuator + Micrometer + Prometheus + Grafana |
| **Logging** | JSON logs + ELK/CloudWatch |
| **Tracing** | OpenTelemetry + Jaeger/Zipkin |
| **Scaling** | K8s HPA, AWS Auto Scaling Groups |
| **Cost** | Spot Instances, Reserved Instances, Right-sizing |

> 💡 **Interview Gold:** "In production, we follow the 12-Factor App principles — externalized config, graceful shutdown, structured JSON logging to CloudWatch, metrics via Prometheus/Grafana, and Kubernetes HPA for auto-scaling based on CPU and custom metrics."
