# 🚀 FinSight AI — Intelligent Trading & Finance Platform

> **Full-stack demo project** | Spring Boot 3 · Spring AI · RAG · pgvector · React · Docker

[![Build Status](https://github.com/your-org/finsight-ai/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/your-org/finsight-ai/actions)

---

## 📋 What This Demonstrates

| Skill | Implementation |
|-------|---------------|
| **Spring Boot 3** | REST APIs, JPA, Security, Actuator |
| **JWT Authentication** | Stateless auth with JJWT 0.12 |
| **Spring AI** | OpenAI chat + embedding integration |
| **RAG Pipeline** | Document ingestion → chunking → pgvector → retrieval → LLM |
| **Multi-Agent System** | 4 specialized agents (Market, Sentiment, Fundamental, Risk) |
| **pgvector** | Cosine similarity vector search in PostgreSQL |
| **Redis** | Conversation memory (session context) + response caching |
| **React + Vite** | Dark-theme trading UI with live market data |
| **Docker Compose** | Full-stack local setup with one command |
| **Kubernetes** | Production deployment manifests with HPA |
| **GitHub Actions** | Full CI/CD pipeline: test → build → push → deploy |

---

## ⚡ Quick Start (Docker Compose)

### Prerequisites
- Docker Desktop installed
- OpenAI API key

### 1. Clone and configure
```bash
git clone https://github.com/your-org/finsight-ai.git
cd finsight-ai
```

Create a `.env` file:
```env
OPENAI_API_KEY=sk-your-openai-key-here
JWT_SECRET=FinSightAISecretKey2026VeryLongSecretKeyForHS256Algorithm!
```

### 2. Start everything
```bash
docker-compose up --build
```

Wait ~90 seconds for Spring Boot to start (first run downloads images + runs Flyway migrations).

### 3. Open the app
| Service | URL |
|---------|-----|
| **Frontend** | http://localhost:3000 |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **API** | http://localhost:8080/api |
| **Health** | http://localhost:8080/actuator/health |

### 4. Demo credentials
```
Email:    demo@finsight.ai
Password: Demo@1234
```

---

## 💻 Local Development (Without Docker)

### Backend
```bash
# Start PostgreSQL with pgvector and Redis
docker-compose up postgres redis -d

# Run Spring Boot
cd backend
export OPENAI_API_KEY=sk-your-key
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

---

## 🎯 Demo Walkthrough

### 1. Login
- Use demo credentials or register a new account
- JWT token stored in localStorage

### 2. Dashboard
- Live Nifty 50, Sensex, Bank Nifty indices (auto-refresh every 15s)
- Top 10 trending NSE stocks with simulated prices

### 3. Portfolio
- Add stocks: e.g., TCS, INFY, HDFCBANK with quantity and buy price
- See real-time P&L calculated against simulated live prices
- Color-coded gains (green) / losses (red)

### 4. AI Chat (RAG-powered)
- Ask: *"What is the KYC requirement for NRI accounts?"*
- Ask: *"Explain systematic investment plans"*
- RAG pipeline: query → embed → pgvector search → assemble context → GPT-4o-mini → grounded response
- Session memory keeps last 10 messages in Redis

### 5. AI Research Agent
- Select a stock (e.g., HDFCBANK)
- Click "Run Analysis"
- Watch 4 agents run: Market Data → Sentiment → Fundamentals → Risk
- Expand each agent to see its output
- Get a full professional research report

---

## 🏗️ Architecture

```
[React Frontend]  ──► [Spring Boot API :8080]
                              │
                    ┌─────────┼──────────────┐
                    ▼         ▼              ▼
               [PostgreSQL] [Redis]    [OpenAI API]
               + pgvector  (sessions)  (GPT-4o-mini
               (vector DB)             embeddings)
```

### Key AI Flow (RAG)
```
User Question
    │
    ▼
OpenAI Embedding (text-embedding-3-small)
    │
    ▼
pgvector Cosine Similarity Search (top-5 chunks)
    │
    ▼
Prompt Assembly: System + Context + Question
    │
    ▼
GPT-4o-mini → Grounded Answer
```

### Multi-Agent Pipeline
```
Orchestrator
    ├── Agent 1: Market Data (live prices via MarketService)
    ├── Agent 2: News Sentiment (GPT generates sentiment analysis)
    ├── Agent 3: Fundamental Analysis (RAG on financial docs)
    └── Agent 4: Risk Assessment (GPT risk scoring)
         │
         ▼
Synthesis Agent → Full Research Report
```

---

## 📡 API Reference

### Authentication
```http
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me
```

### Portfolio
```http
GET    /api/portfolio
POST   /api/portfolio/holdings
DELETE /api/portfolio/holdings/{id}
GET    /api/portfolio/transactions
```

### Market Data
```http
GET /api/market/quote/{symbol}
GET /api/market/search?q=
GET /api/market/trending
GET /api/market/indices
```

### AI
```http
POST /api/ai/chat
GET  /api/ai/chat/history/{sessionId}
DELETE /api/ai/chat/history/{sessionId}
POST /api/ai/research/{symbol}
GET  /api/ai/research/history
GET  /api/documents/semantic-search?q=
```

### Documents
```http
POST /api/documents/upload  (multipart/form-data)
GET  /api/documents
```

---

## 🐳 Production Deployment (AWS EKS)

```bash
# Create namespace
kubectl create namespace finsight

# Create secrets
kubectl create secret generic finsight-secrets \
  --from-literal=openai-api-key=sk-your-key \
  --from-literal=jwt-secret=your-secret \
  -n finsight

# Deploy
kubectl apply -f infrastructure/kubernetes/deployment.yaml
```

---

## 🔧 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.3, Java 17 |
| AI | Spring AI 1.0, OpenAI GPT-4o-mini |
| Auth | Spring Security + JWT (JJWT 0.12) |
| ORM | Spring Data JPA + Hibernate |
| Vector DB | PostgreSQL 16 + pgvector |
| Cache | Redis 7 |
| Frontend | React 18, Vite, TypeScript, Tailwind CSS |
| Container | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Cloud | AWS EKS + ALB + RDS + ElastiCache |
| Monitoring | Spring Actuator + Prometheus + Grafana |

---

## 📁 Project Structure

```
finsight-ai/
├── backend/                    Spring Boot application
│   ├── src/main/java/com/finsight/
│   │   ├── auth/               JWT authentication
│   │   ├── portfolio/          Portfolio management
│   │   ├── market/             Simulated market data
│   │   ├── ai/
│   │   │   ├── rag/            RAG pipeline
│   │   │   ├── chat/           AI chatbot with memory
│   │   │   ├── agent/          Multi-agent research
│   │   │   └── document/       Document ingestion
│   │   └── config/             Security, Redis, OpenAPI
│   └── src/main/resources/
│       └── db/migration/       Flyway SQL migrations
├── frontend/                   React + Vite + Tailwind
│   └── src/
│       ├── pages/              Login, Dashboard, Portfolio, Chat, Research
│       ├── components/         Layout, reusable UI
│       └── api/                Axios client
├── infrastructure/
│   ├── kubernetes/             K8s deployment manifests
│   └── .github/workflows/     GitHub Actions CI/CD
└── docker-compose.yml
```

---

## 🎤 Interview Talking Points

This project demonstrates:

1. **"How do you integrate AI in Spring Boot?"** → Spring AI with ChatClient/EmbeddingModel, RAG pipeline with pgvector VectorStore
2. **"How do you implement RAG?"** → Document ingestion → chunking → OpenAI embedding → pgvector storage → cosine similarity search → prompt assembly → GPT response
3. **"What is an AI agent?"** → ResearchAgentService: 4-agent pipeline with tool calling (MarketService, RagService, direct LLM)
4. **"How do you manage LLM context?"** → Redis-backed conversation history, token budget control in RagService, max context window enforcement
5. **"How do you deploy to cloud?"** → Docker → GitHub Actions → ECR → EKS with HPA and health probes

> 💡 Every component of this project maps directly to a concept in the AI.md, springboot-cloud.md, and springboot-ai.md study guides.
