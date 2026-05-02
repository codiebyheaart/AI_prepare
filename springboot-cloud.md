# Spring Boot + Cloud — Deployment & Monitoring Guide

> Deploy, monitor, and scale Spring Boot applications on AWS / Azure / GCP

---

## 1. Docker — Containerizing Spring Boot

### Optimized Multi-Stage Dockerfile

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN ./mvnw dependency:resolve
COPY src ./src
RUN ./mvnw package -DskipTests

# Stage 2: Run (minimal image)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

### Docker Compose (Full Stack)

```yaml
version: '3.8'
services:
  app:
    build: .
    ports: ["8080:8080"]
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: postgres
      REDIS_HOST: redis
    depends_on:
      postgres: { condition: service_healthy }
      redis: { condition: service_started }

  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: tradingdb
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes: ["pgdata:/var/lib/postgresql/data"]
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U admin"]
      interval: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  prometheus:
    image: prom/prometheus
    volumes: ["./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml"]
    ports: ["9090:9090"]

  grafana:
    image: grafana/grafana
    ports: ["3000:3000"]
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin

volumes:
  pgdata:
```

---

## 2. Kubernetes Deployment

### Deployment + Service + Ingress

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: trading-api
  labels:
    app: trading-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: trading-api
  template:
    metadata:
      labels:
        app: trading-api
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      containers:
        - name: trading-api
          image: myregistry/trading-api:1.0.0
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: password
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 20
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 15
          startupProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            failureThreshold: 30
            periodSeconds: 5

---
apiVersion: v1
kind: Service
metadata:
  name: trading-api-svc
spec:
  selector:
    app: trading-api
  ports:
    - port: 80
      targetPort: 8080
  type: ClusterIP

---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: trading-api-ingress
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
    - hosts: ["api.trading.com"]
      secretName: trading-tls
  rules:
    - host: api.trading.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: trading-api-svc
                port:
                  number: 80
```

### Horizontal Pod Autoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: trading-api-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: trading-api
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
```

---

## 3. AWS Deployment Options

### Option A: ECS (Fargate) — Serverless Containers

```bash
# Build and push to ECR
aws ecr get-login-password | docker login --username AWS --password-stdin <account>.dkr.ecr.us-east-1.amazonaws.com
docker build -t trading-api .
docker tag trading-api:latest <account>.dkr.ecr.us-east-1.amazonaws.com/trading-api:latest
docker push <account>.dkr.ecr.us-east-1.amazonaws.com/trading-api:latest
```

```json
// ecs-task-definition.json
{
  "family": "trading-api",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [{
    "name": "trading-api",
    "image": "<account>.dkr.ecr.us-east-1.amazonaws.com/trading-api:latest",
    "portMappings": [{"containerPort": 8080}],
    "environment": [
      {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"}
    ],
    "secrets": [
      {"name": "DB_PASSWORD", "valueFrom": "arn:aws:secretsmanager:us-east-1:<account>:secret:db-password"}
    ],
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": {
        "awslogs-group": "/ecs/trading-api",
        "awslogs-region": "us-east-1",
        "awslogs-stream-prefix": "ecs"
      }
    },
    "healthCheck": {
      "command": ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"],
      "interval": 30,
      "timeout": 5,
      "retries": 3
    }
  }]
}
```

### Option B: Elastic Beanstalk (Simplest)

```bash
# Install EB CLI and deploy
eb init trading-api --platform "corretto-17" --region us-east-1
eb create trading-prod --instance_type t3.medium --envvars SPRING_PROFILES_ACTIVE=prod
eb deploy
```

### AWS S3 Integration (File Storage)

```java
@Service
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file) {
        String key = "documents/" + UUID.randomUUID() + "/" + file.getOriginalFilename();

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build(),
            RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        return key;
    }

    public byte[] downloadFile(String key) {
        return s3Client.getObjectAsBytes(
            GetObjectRequest.builder().bucket(bucket).key(key).build()
        ).asByteArray();
    }
}
```

---

## 4. Azure Deployment

### Azure Kubernetes Service (AKS)

```bash
# Create AKS cluster
az aks create -g myResourceGroup -n trading-cluster --node-count 3 --enable-addons monitoring
az aks get-credentials -g myResourceGroup -n trading-cluster

# Deploy
kubectl apply -f k8s/
```

### Azure Blob Storage

```java
@Service
public class AzureBlobService {
    private final BlobServiceClient blobServiceClient;

    public String upload(MultipartFile file) {
        BlobContainerClient container = blobServiceClient.getBlobContainerClient("documents");
        String blobName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        BlobClient blob = container.getBlobClient(blobName);
        blob.upload(file.getInputStream(), file.getSize(), true);
        return blob.getBlobUrl();
    }
}
```

---

## 5. GCP Deployment

### Cloud Run (Serverless)

```bash
# Build with Cloud Build and deploy to Cloud Run
gcloud builds submit --tag gcr.io/my-project/trading-api
gcloud run deploy trading-api \
  --image gcr.io/my-project/trading-api \
  --port 8080 \
  --memory 1Gi \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod \
  --allow-unauthenticated
```

---

## 6. CI/CD Pipeline (GitHub Actions)

```yaml
# .github/workflows/deploy.yml
name: Build & Deploy

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      - run: mvn verify

  build-and-push:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      - uses: docker/build-push-action@v5
        with:
          push: true
          tags: ghcr.io/${{ github.repository }}/trading-api:${{ github.sha }}

  deploy:
    needs: build-and-push
    runs-on: ubuntu-latest
    steps:
      - uses: azure/k8s-set-context@v3
        with:
          kubeconfig: ${{ secrets.KUBE_CONFIG }}
      - run: |
          kubectl set image deployment/trading-api \
            trading-api=ghcr.io/${{ github.repository }}/trading-api:${{ github.sha }}
          kubectl rollout status deployment/trading-api
```

---

## 7. Monitoring & Observability

### Spring Boot Actuator + Prometheus + Grafana

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    tags:
      application: trading-api
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

```yaml
# monitoring/prometheus.yml
scrape_configs:
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['app:8080']
```

### Custom Business Metrics

```java
@Component
public class TradingMetrics {
    private final Counter tradesExecuted;
    private final Timer tradeLatency;
    private final Gauge activePositions;

    public TradingMetrics(MeterRegistry registry) {
        this.tradesExecuted = Counter.builder("trades.executed")
            .description("Total trades executed")
            .register(registry);

        this.tradeLatency = Timer.builder("trades.latency")
            .description("Trade execution time")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry);

        this.activePositions = Gauge.builder("positions.active",
            positionRepository, repo -> repo.countActive())
            .description("Active trading positions")
            .register(registry);
    }

    public void recordTrade(Runnable tradeExecution) {
        tradeLatency.record(tradeExecution);
        tradesExecuted.increment();
    }
}
```

---

## 8. Distributed Tracing (OpenTelemetry)

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-zipkin</artifactId>
</dependency>
```

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% in dev, lower in prod
  zipkin:
    tracing:
      endpoint: http://zipkin:9411/api/v2/spans
```

```java
// Traces propagate automatically across REST calls, Kafka, etc.
// Add custom spans for business logic
@Service
public class OrderService {
    private final Tracer tracer;

    public Order processOrder(OrderRequest req) {
        Span span = tracer.nextSpan().name("process-order").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            span.tag("order.type", req.getType());
            // ... business logic
            return order;
        } finally {
            span.end();
        }
    }
}
```

---

## 9. ELK Stack (Centralized Logging)

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>spanId</includeMdcKeyName>
        </encoder>
    </appender>

    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>logstash:5000</destination>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="LOGSTASH"/>
    </root>
</configuration>
```

---

## 10. Terraform (Infrastructure as Code)

```hcl
# main.tf — AWS ECS Infrastructure
provider "aws" {
  region = "us-east-1"
}

resource "aws_ecs_cluster" "trading" {
  name = "trading-cluster"
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_ecs_service" "trading_api" {
  name            = "trading-api"
  cluster         = aws_ecs_cluster.trading.id
  task_definition = aws_ecs_task_definition.trading_api.arn
  desired_count   = 3
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = var.private_subnet_ids
    security_groups = [aws_security_group.ecs_sg.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.trading.arn
    container_name   = "trading-api"
    container_port   = 8080
  }
}

resource "aws_rds_instance" "trading_db" {
  identifier     = "trading-db"
  engine         = "postgres"
  engine_version = "16.1"
  instance_class = "db.t3.medium"
  allocated_storage = 50
  db_name        = "tradingdb"
  username       = var.db_username
  password       = var.db_password
  skip_final_snapshot = true
}
```

---

## Quick Reference — Cloud Interview Questions

| Question | Answer |
|----------|--------|
| Docker vs VM? | Docker shares host OS kernel (lightweight); VM has full guest OS |
| K8s Pod vs Container? | Pod = smallest deployable unit, can hold 1+ containers sharing network |
| Liveness vs Readiness probe? | Liveness = restart if fails; Readiness = stop traffic if fails |
| Blue/Green vs Canary? | Blue/Green = instant switch; Canary = gradual rollout % |
| 12-Factor App principles? | Config in env, stateless processes, port binding, disposability, etc. |
| How to handle secrets in K8s? | K8s Secrets + Vault + External Secrets Operator |
| Cloud Run vs ECS Fargate? | Both serverless containers; Cloud Run = simpler, GCP; Fargate = AWS |

---

*Next: `AI.md` →*
