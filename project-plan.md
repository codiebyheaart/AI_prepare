# 🏗️ AI-Powered Trading & Finance Platform — Project Plan

> **Full-Stack Project | Spring Boot + AI + Cloud | Trading/Finance Domain**
> **Covers:** LLM, RAG, LangChain, Agents, Vector DB, Cloud, 3rd Party APIs

---

## 1. Project Overview

**Name:** FinSight AI — Intelligent Trading & Portfolio Assistant

**What it does:**
- Real-time stock tracking with AI-powered analysis
- AI chatbot for market Q&A (RAG-grounded)
- Portfolio management with AI-driven insights
- Automated alerts using AI sentiment analysis
- Multi-agent research system for stock recommendations

**Integrations:**
- Zerodha Kite API — real-time stock data and order placement
- NSE/BSE market feeds
- OpenAI / Gemini for LLM
- pgvector for vector search
- AWS S3 for document storage
- AWS for cloud deployment

---

## 2. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        FINSIGHT AI PLATFORM                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────┐        ┌─────────────────────────────┐    │
│  │   FRONTEND        │        │    API GATEWAY               │    │
│  │  (React / Next.js)│◄──────►│  (Spring Cloud Gateway)     │    │
│  │                  │        │  Rate limiting, Auth, Routing│    │
│  └──────────────────┘        └─────────────┬───────────────┘    │
│                                             │                     │
│                    ┌────────────────────────┼────────────────┐   │
│                    ▼                        ▼                ▼   │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────────────────┐      │
│  │  User &      │ │  Portfolio   │ │   AI Orchestration   │      │
│  │  Auth        │ │  Service     │ │   Service            │      │
│  │  Service     │ │              │ │  (Spring Boot + AI)  │      │
│  └──────┬───────┘ └──────┬───────┘ └──────────┬──────────┘      │
│         │                │                     │                  │
│         ▼                ▼                     ▼                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    DATA LAYER                             │   │
│  │   PostgreSQL+pgvector │ Redis │ MongoDB │ AWS S3          │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                EXTERNAL INTEGRATIONS                      │   │
│  │   Zerodha Kite API │ OpenAI │ Gemini │ NSE/BSE Feed       │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Microservices Breakdown

### Service 1 — User & Auth Service
- User registration / login
- JWT token issuance and validation
- Role management: ADMIN, TRADER, VIEWER
- Integration: Spring Security + JWT

### Service 2 — Portfolio Service
- Manage user portfolios (add/remove stocks)
- Calculate P&L (profit & loss) in real-time
- Holdings, transaction history
- Integration: PostgreSQL + Zerodha API

### Service 3 — Market Data Service
- Fetch real-time stock prices (Zerodha Kite WebSocket)
- Historical OHLCV data
- Market indices: Nifty 50, Sensex, Bank Nifty
- Push via Kafka to other services

### Service 4 — AI Orchestration Service *(Most Important)*
- RAG-powered knowledge base chatbot
- Multi-agent stock research
- Sentiment analysis on news
- Personalized recommendations
- All LLM interactions, vector DB queries

### Service 5 — Notification Service
- Kafka consumer for alerts
- Email/SMS/push notifications
- Price alerts, AI-generated daily summaries

### Service 6 — Document Service
- Upload and manage financial documents (reports, prospectus)
- Store in AWS S3
- Extract text, chunk, embed into vector DB
- Powers RAG knowledge base

---

## 4. AI Features — Detailed Design

### 4.1 AI Chatbot (RAG-Powered)

**What it does:** Answer user questions about stocks, markets, and company policies grounded in real documents.

**Flow:**
```
[User: "What was Infosys revenue in Q4 FY2026?"]
                    │
                    ▼
       [Query Embedding] (text-embedding-3-small)
                    │
                    ▼
  [pgvector Similarity Search]
  (Search in ingested: earnings reports, news, analyst reports)
                    │
                    ▼
  [Top 5 relevant document chunks retrieved]
                    │
                    ▼
  [Prompt Assembly]
  System: "You are FinSight, a financial analyst assistant.
           Answer only from the provided context.
           If unsure, say you don't know."
  Context: [5 retrieved chunks]
  User: "What was Infosys revenue in Q4 FY2026?"
                    │
                    ▼
  [GPT-4o / Gemini Pro]
                    │
                    ▼
  [Grounded Answer with source citations]
  "Based on Infosys Q4 FY2026 earnings report,
   total revenue was ₹37,923 Cr, up 7.2% YoY..."
```

**Token Control:**
- Limit retrieved chunks: max 3,000 tokens of context
- System prompt: max 500 tokens
- Reserve 1,000 tokens for response
- Total: < 5,000 tokens per query (cost control)

---

### 4.2 Multi-Agent Stock Research System

```
[User: "Give me a detailed analysis of HDFC Bank for investment"]
                         │
                         ▼
              [ORCHESTRATOR AGENT]
              Decomposes task into sub-tasks
                         │
     ┌───────────────────┼────────────────────┐
     ▼                   ▼                    ▼
[MARKET DATA         [NEWS &              [FUNDAMENTAL
 AGENT]               SENTIMENT            ANALYSIS
 • Zerodha API:       AGENT]               AGENT]
   current price    • Search latest       • RAG search:
   52-week H/L        news (Google)         annual reports
   volume data      • Sentiment:           • P/E ratio
   technicals         Bullish/Bearish      • Revenue trend
                    • Key events           • Debt analysis
     │                   │                    │
     └───────────────────┼────────────────────┘
                         ▼
              [SYNTHESIS AGENT]
              Merges all data into structured analysis
                         │
                         ▼
              [RISK ASSESSMENT AGENT]
              (Compares to user's portfolio risk profile)
                         │
                         ▼
              [REPORT WRITER AGENT]
              (LLM generates investment report)
                         │
                         ▼
              [Final 500-word analysis report]
```

---

### 4.3 Semantic Stock Search

```
[User types: "show me profitable healthcare stocks with low debt"]
                    │
                    ▼
       [Embed the query text]
                    │
                    ▼
  [pgvector search on stock metadata embeddings]
  (Each stock's description, financials, sector embedded)
                    │
                    ▼
  [Return top 10 semantically similar stocks]
  (Goes beyond keyword matching — finds intent)
```

### 4.4 AI News Sentiment Engine

```
[Kafka: New article from NSE/BSE/Economic Times]
                    │
                    ▼
  [AI Classification]
  → Sector affected (Banking, IT, Auto...)
  → Company mentioned (Infosys, TCS, HDFC...)
  → Sentiment (-1 bearish to +1 bullish)
  → Impact score (1-10)
                    │
                    ▼
  [Store in PostgreSQL]
                    │
                    ▼
  [Trigger alerts if: stock in user portfolio + high impact]
```

### 4.5 AI Daily Portfolio Summary (Scheduled)

```
[Scheduled: 7 PM every trading day]
                    │
                    ▼
[Fetch: User portfolio performance for the day]
[Fetch: Market indices performance]
[Fetch: News sentiment scores for held stocks]
                    │
                    ▼
[LLM: Generate personalized daily summary]
"Today your portfolio gained 1.2% (₹4,200).
 TCS contributed most at +2.3%. Infosys lagged
 at -0.8% due to guidance revision. Market
 sentiment for IT sector remains cautiously
 optimistic..."
                    │
                    ▼
[Email/Push notification to user]
```

---

## 5. Data Architecture

### 5.1 PostgreSQL (Primary DB + Vector)
```
Tables:
  users          → id, email, password_hash, role, created_at
  portfolios     → id, user_id, name, created_at
  holdings       → id, portfolio_id, symbol, quantity, avg_price
  transactions   → id, portfolio_id, symbol, type, price, qty, timestamp
  price_alerts   → id, user_id, symbol, target_price, condition, active
  
pgvector Tables:
  document_chunks → id, content, embedding (vector 1536), source, metadata
  stock_embeddings → id, symbol, description, embedding (vector 1536)
  news_embeddings  → id, headline, content, embedding, sentiment, date
```

### 5.2 Redis (Cache + Session)
```
Keys:
  price:{symbol}          → Real-time price (TTL: 15 seconds)
  portfolio:{userId}      → Computed P&L (TTL: 30 seconds)
  session:{token}         → JWT session data (TTL: 24 hours)
  sentiment:{symbol}      → Aggregated sentiment (TTL: 1 hour)
  ai:response:{hash}      → Cached AI responses (TTL: 5 minutes)
```

### 5.3 MongoDB (Event Log)
```
Collections:
  ai_interactions   → {userId, query, response, tokens, cost, timestamp}
  market_events     → {symbol, event_type, description, impact, timestamp}
  audit_log         → {userId, action, entity, old_value, new_value, timestamp}
```

### 5.4 AWS S3 (Document Storage)
```
Buckets:
  finsight-documents/
    ├── annual-reports/      ← Company annual reports (PDF)
    ├── earnings/            ← Quarterly earnings (PDF)
    ├── research-reports/    ← Analyst research reports
    └── user-uploads/        ← User-uploaded documents
```

---

## 6. Token Control & Cost Management

### Token Budget Strategy
```
Per AI Request:
  System prompt:        ~300 tokens
  Retrieved context:    ~2,000 tokens (RAG)
  User message:         ~100 tokens
  Response buffer:      ~800 tokens
  Total per call:       ~3,200 tokens

Cost (GPT-4o):
  Input:  ~$0.005 per 1K tokens → 2,400 tokens = $0.012
  Output: ~$0.015 per 1K tokens → 800 tokens = $0.012
  Per call: ~$0.024

At 10,000 calls/day → $240/day → ~$7,200/month
```

### Cost Optimization
- Cache common AI responses in Redis (TTL: 5 min)
- Use GPT-4o Mini for simple classification (10x cheaper)
- Use Gemini Flash for bulk tasks (very cheap, fast)
- Use local Ollama (Llama 3) for development/testing
- Monitor token usage via Micrometer custom metrics

---

## 7. Third-Party Integrations

### Zerodha Kite API
```
REST Endpoints:
  GET  /quote         → Get stock price
  GET  /historical    → OHLCV historical data
  POST /orders        → Place buy/sell order
  GET  /portfolio     → Get holdings

WebSocket (KiteTicker):
  Connect → Subscribe to stock symbols
  Receive: real-time tick data (price, volume)
  Publish to Kafka → Consumed by services
```

### Market Data Alternatives
- **NSE India API** — Free, limited
- **Yahoo Finance** — Free, rate-limited
- **Alpha Vantage** — Free tier available
- **TrueData / Global Data Feeds** — Paid, real-time

---

## 8. Cloud Deployment Architecture (AWS)

```
┌─────────────────────────────────────────────────────────┐
│                      AWS CLOUD                           │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │                    VPC                            │   │
│  │                                                   │   │
│  │  ┌─────────┐   ┌──────────────────────────────┐  │   │
│  │  │   ALB   │──►│         EKS Cluster           │  │   │
│  │  │ (HTTPS) │   │  ┌──────────┐ ┌───────────┐  │  │   │
│  │  └─────────┘   │  │ Auth Pod │ │ AI Pod    │  │  │   │
│  │                │  │ (2 rep.) │ │ (3 rep.)  │  │  │   │
│  │                │  └──────────┘ └───────────┘  │  │   │
│  │                │  ┌──────────┐ ┌───────────┐  │  │   │
│  │                │  │Portfolio │ │ Market    │  │  │   │
│  │                │  │ Pod(2)   │ │ Data Pod  │  │  │   │
│  │                │  └──────────┘ └───────────┘  │  │   │
│  │                └──────────────────────────────┘  │   │
│  │                                                   │   │
│  │  ┌─────────────────────────────────────────┐     │   │
│  │  │           DATA SERVICES                  │     │   │
│  │  │  RDS PostgreSQL │ ElastiCache │ MSK Kafka│     │   │
│  │  └─────────────────────────────────────────┘     │   │
│  │                                                   │   │
│  │  ┌────────────┐  ┌──────────┐  ┌──────────────┐  │   │
│  │  │  S3 Bucket │  │CloudWatch│  │  Secrets Mgr │  │   │
│  │  └────────────┘  └──────────┘  └──────────────┘  │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### CI/CD Pipeline
```
[GitHub Push to main]
        │
        ▼
[GitHub Actions]
  1. Run tests (Maven test)
  2. Build Docker images
  3. Push to ECR
  4. Deploy to EKS (kubectl apply)
        │
        ▼
[ArgoCD GitOps]
  (Syncs K8s manifests from Git)
        │
        ▼
[Slack: Deployment notification]
```

---

## 9. Project Folder Structure

```
finsight-ai/
├── frontend/                    ← React / Next.js
│   ├── pages/
│   │   ├── dashboard.tsx
│   │   ├── portfolio.tsx
│   │   ├── chat.tsx             ← AI chatbot UI
│   │   └── research.tsx         ← AI research reports
│   └── components/
│
├── backend/
│   ├── api-gateway/             ← Spring Cloud Gateway
│   ├── auth-service/            ← Spring Boot + Spring Security
│   ├── portfolio-service/       ← Spring Boot + JPA
│   ├── market-data-service/     ← Spring Boot + Kafka
│   ├── ai-service/              ← Spring Boot + Spring AI + LangChain4j
│   │   ├── rag/                 ← RAG pipeline
│   │   ├── agents/              ← Agent definitions
│   │   ├── tools/               ← Tool implementations
│   │   └── memory/              ← Conversation memory
│   ├── notification-service/    ← Spring Boot + Kafka consumer
│   └── document-service/        ← Spring Boot + AWS S3
│
├── infrastructure/
│   ├── kubernetes/              ← K8s manifests
│   ├── terraform/               ← Infrastructure as code (AWS)
│   └── docker-compose.yml       ← Local development
│
└── docs/
    ├── architecture.md
    ├── api-docs/
    └── deployment-guide.md
```

---

## 10. Key Technical Decisions & Trade-offs

| Decision | Choice | Why |
|----------|--------|-----|
| **Vector DB** | pgvector (PostgreSQL extension) | Avoid new infra; PostgreSQL already in use; good for < 1M vectors |
| **LLM Provider** | OpenAI GPT-4o primary, Gemini Flash fallback | GPT-4o best quality; Gemini Flash for cost-sensitive bulk tasks |
| **Message Broker** | Kafka (MSK) | Durable, high-throughput, Zerodha tick data volume |
| **Cache** | Redis (ElastiCache) | Price caching, session, AI response cache |
| **Container Orchestration** | EKS (Kubernetes) | Portability, HPA, rolling deployments |
| **AI Framework** | Spring AI + LangChain4j | Spring AI for simple tasks; LangChain4j for complex agents |
| **Auth** | JWT stateless | Scales horizontally without session store dependency |
| **Secrets** | AWS Secrets Manager | No hardcoded secrets; auto-rotation |

---

## 11. Monitoring & Observability

```
Metrics:   Actuator → Prometheus → Grafana
           (AI metrics: token usage, LLM latency, cost per request)

Logs:      JSON logs → CloudWatch Logs → Log Insights queries
           (Include: traceId, userId, symbol, AI model used)

Traces:    OpenTelemetry → AWS X-Ray
           (Trace request from API Gateway → AI Service → Vector DB → LLM)

Alerts:
  - LLM error rate > 2% → PagerDuty
  - AI latency p99 > 5s → Slack alert
  - Daily AI cost > $300 → Email to team
  - Service health check fails → Auto-restart + alert
```

---

## 12. Sprint Plan (8-Week MVP)

| Week | Goal |
|------|------|
| **1** | Setup: Monorepo, Docker Compose, Auth service, basic React app |
| **2** | Portfolio service: holdings, P&L, Zerodha API integration |
| **3** | Market data: WebSocket feed, Kafka pipeline, price caching |
| **4** | AI foundation: RAG pipeline, vector DB, document ingestion |
| **5** | AI chatbot: chatbot UI, context management, streaming responses |
| **6** | Multi-agent system: stock research agents, LangGraph workflows |
| **7** | Cloud: EKS deployment, CI/CD, monitoring setup |
| **8** | Polish: Performance, cost optimization, documentation, demo |

---

## Summary

**FinSight AI** demonstrates the complete intersection of:

| Technology | Role in Project |
|-----------|----------------|
| **Spring Boot** | All backend microservices |
| **Spring AI / LangChain4j** | AI orchestration layer |
| **LLM (GPT-4o)** | Natural language understanding and generation |
| **RAG** | Ground AI answers in real financial documents |
| **LangGraph** | Multi-step agent workflows |
| **Multi-Agent** | Specialized research agents working together |
| **pgvector** | Semantic search on financial data |
| **Zerodha Kite** | Real trading data and order management |
| **Kafka** | Real-time market data streaming |
| **Redis** | Caching prices, sessions, AI responses |
| **AWS EKS** | Kubernetes deployment |
| **AWS S3** | Document storage |
| **GitHub Actions + ArgoCD** | CI/CD automation |
| **React/Next.js** | Frontend UI |

> 💡 **For your interview:** This project alone can carry 30-40 minutes of deep technical conversation covering Spring Boot architecture, AI patterns, cloud deployment, system design, and real-world problem solving. Know every component, every trade-off, and every line of the architecture diagram.
