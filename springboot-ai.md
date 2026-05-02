# Spring Boot + AI Integration — Interview Guide

> Real-world examples in **Banking, Trading & Finance** domains

---

## 1. Spring AI Framework Overview

Spring AI provides a unified API to integrate LLMs into Spring Boot apps.

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
</dependency>
```

```yaml
# application.yml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
          temperature: 0.3
    vectorstore:
      pgvector:
        dimensions: 1536
```

---

## 2. Basic Chat Completion

```java
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final ChatClient chatClient;

    public AiChatController(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultSystem("You are a senior financial advisor. Be concise and data-driven.")
            .build();
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String userMessage) {
        return chatClient.prompt()
            .user(userMessage)
            .call()
            .content();
    }

    // Streaming response
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String query) {
        return chatClient.prompt()
            .user(query)
            .stream()
            .content();
    }
}
```

---

## 3. Structured Output (JSON from LLM)

```java
// Define response structure
public record StockAnalysis(
    String ticker,
    String recommendation,  // BUY, SELL, HOLD
    double targetPrice,
    String reasoning,
    List<String> risks
) {}

@Service
public class StockAnalysisService {

    private final ChatClient chatClient;

    public StockAnalysis analyzeStock(String ticker, String financialData) {
        return chatClient.prompt()
            .system("Analyze stocks and return structured recommendations.")
            .user(u -> u.text("""
                Analyze {ticker} based on this financial data:
                {data}
                Provide recommendation, target price, reasoning, and risks.
                """)
                .param("ticker", ticker)
                .param("data", financialData))
            .call()
            .entity(StockAnalysis.class);  // Auto-parses to Java object
    }
}
```

---

## 4. Function Calling / Tool Use

LLMs can invoke your Java methods to fetch real-time data.

```java
// Define tools the LLM can call
@Service
public class TradingTools {

    @Tool(description = "Get current stock price for a given ticker symbol")
    public StockPrice getCurrentPrice(@ToolParam("Stock ticker e.g. AAPL") String ticker) {
        // Call real API (Zerodha, Alpha Vantage, etc.)
        return stockApiClient.getQuote(ticker);
    }

    @Tool(description = "Get account portfolio holdings")
    public List<Holding> getPortfolio(@ToolParam("Account ID") String accountId) {
        return portfolioRepository.findByAccountId(accountId);
    }

    @Tool(description = "Place a stock order")
    public OrderConfirmation placeOrder(
            @ToolParam("Ticker") String ticker,
            @ToolParam("BUY or SELL") String side,
            @ToolParam("Number of shares") int quantity) {
        return tradingService.executeOrder(ticker, side, quantity);
    }
}

// Use tools with ChatClient
@Service
public class TradingAssistant {

    private final ChatClient chatClient;
    private final TradingTools tradingTools;

    public String chat(String userQuery) {
        return chatClient.prompt()
            .system("""
                You are a trading assistant. Use available tools to fetch
                real-time data before making recommendations. Always confirm
                before placing orders.
                """)
            .user(userQuery)
            .tools(tradingTools)
            .call()
            .content();
    }
}
```

---

## 5. RAG Pipeline in Spring Boot (Banking Document Q&A)

```
┌──────────┐    ┌──────────────┐    ┌────────────┐    ┌─────────┐
│ PDF/Docs │───▶│ Text Splitter│───▶│ Embeddings │───▶│ PGVector│
└──────────┘    └──────────────┘    └────────────┘    └─────────┘
                                                           │
┌──────────┐    ┌──────────────┐    ┌────────────┐         │
│ Response │◀───│   LLM Call   │◀───│  Context   │◀────────┘
└──────────┘    └──────────────┘    └────────────┘
```

### Step 1: Ingest Documents

```java
@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public void ingestPdf(Resource pdfResource) {
        // Parse PDF
        var reader = new PagePdfDocumentReader(pdfResource);
        List<Document> documents = reader.read();

        // Split into chunks
        var splitter = new TokenTextSplitter(800, 200, 5, 10000, true);
        List<Document> chunks = splitter.apply(documents);

        // Add metadata
        chunks.forEach(doc -> {
            doc.getMetadata().put("source", pdfResource.getFilename());
            doc.getMetadata().put("type", "banking-policy");
        });

        // Store embeddings in vector DB
        vectorStore.add(chunks);
    }
}
```

### Step 2: Query with RAG

```java
@Service
public class BankingQAService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public String askQuestion(String question) {
        // Retrieve relevant documents
        var advisor = QuestionAnswerAdvisor.builder(vectorStore)
            .searchRequest(SearchRequest.builder()
                .topK(5)
                .similarityThreshold(0.7)
                .filterExpression("type == 'banking-policy'")
                .build())
            .build();

        return chatClient.prompt()
            .system("""
                You are a banking compliance assistant. Answer questions
                ONLY based on the provided context. If unsure, say so.
                Cite the source document.
                """)
            .user(question)
            .advisors(advisor)
            .call()
            .content();
    }
}
```

### Step 3: REST Endpoint

```java
@RestController
@RequestMapping("/api/banking")
public class BankingController {

    private final DocumentIngestionService ingestionService;
    private final BankingQAService qaService;

    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(@RequestParam MultipartFile file) {
        ingestionService.ingestPdf(file.getResource());
        return ResponseEntity.ok("Document ingested: " + file.getOriginalFilename());
    }

    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestBody String question) {
        return ResponseEntity.ok(qaService.askQuestion(question));
    }
}
```

---

## 6. Embedding Models & Vector Stores

```java
// Using different embedding models
@Configuration
public class EmbeddingConfig {

    // OpenAI embeddings
    @Bean
    @Profile("openai")
    public EmbeddingModel openAiEmbedding() {
        return new OpenAiEmbeddingModel(
            new OpenAiApi(apiKey),
            OpenAiEmbeddingOptions.builder()
                .model("text-embedding-3-small")
                .build());
    }

    // Vector store with PGVector
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, JdbcTemplate jdbc) {
        return PgVectorStore.builder(jdbc, embeddingModel)
            .dimensions(1536)
            .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
            .indexType(PgVectorStore.PgIndexType.HNSW)
            .build();
    }
}

// Similarity search
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("What is the loan approval process?")
        .topK(5)
        .similarityThreshold(0.75)
        .build()
);
```

---

## 7. Multi-Model Support (OpenAI + Gemini + Claude)

```java
@Configuration
public class MultiModelConfig {

    @Bean("openai")
    public ChatClient openAiClient(OpenAiChatModel model) {
        return ChatClient.builder(model)
            .defaultSystem("You are a financial analyst.")
            .build();
    }

    @Bean("gemini")
    public ChatClient geminiClient(VertexAiGeminiChatModel model) {
        return ChatClient.builder(model).build();
    }

    @Bean("claude")
    public ChatClient claudeClient(AnthropicChatModel model) {
        return ChatClient.builder(model).build();
    }
}

// Service that routes to different models
@Service
public class MultiModelService {

    private final Map<String, ChatClient> clients;

    public String query(String model, String prompt) {
        ChatClient client = clients.get(model);
        if (client == null) throw new IllegalArgumentException("Unknown model: " + model);
        return client.prompt().user(prompt).call().content();
    }

    // Consensus approach — ask multiple models
    public ConsensusResult getConsensus(String question) {
        var futures = clients.entrySet().stream()
            .map(e -> CompletableFuture.supplyAsync(() ->
                Map.entry(e.getKey(), e.getValue().prompt().user(question).call().content())))
            .toList();

        var results = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ConsensusResult(results);
    }
}
```

---

## 8. AI Fraud Detection (Banking Example)

```java
@Service
public class FraudDetectionService {

    private final ChatClient chatClient;
    private final TransactionRepository txnRepo;

    public FraudAlert analyzeTransaction(Transaction txn) {
        // Get customer history
        List<Transaction> history = txnRepo.findRecentByCustomer(
            txn.getCustomerId(), 30); // last 30 days

        String historyJson = objectMapper.writeValueAsString(history);

        return chatClient.prompt()
            .system("""
                You are a fraud detection system. Analyze transactions for:
                1. Unusual amounts compared to history
                2. Geographic anomalies
                3. Velocity checks (too many transactions)
                4. Time-of-day anomalies
                Return a risk score (0-100) and explanation.
                """)
            .user(u -> u.text("""
                Current transaction: {txn}
                Customer 30-day history: {history}
                """)
                .param("txn", txn.toString())
                .param("history", historyJson))
            .call()
            .entity(FraudAlert.class);
    }
}

public record FraudAlert(
    int riskScore,
    String riskLevel,      // LOW, MEDIUM, HIGH, CRITICAL
    List<String> flags,
    String recommendation  // APPROVE, REVIEW, BLOCK
) {}
```

---

## 9. AI Trading Assistant (Stock Market Example)

```java
@Service
public class TradingAdvisorService {

    private final ChatClient chatClient;
    private final MarketDataService marketData;

    public TradingAdvice getAdvice(String ticker) {
        // Fetch real data
        StockQuote quote = marketData.getQuote(ticker);
        List<HistoricalPrice> prices = marketData.getHistory(ticker, 90);
        FinancialStatement financials = marketData.getFinancials(ticker);

        return chatClient.prompt()
            .system("""
                You are a quantitative analyst. Provide trading advice based on:
                - Technical analysis (moving averages, RSI, MACD)
                - Fundamental analysis (P/E, revenue growth, margins)
                - Risk assessment
                Always include disclaimers.
                """)
            .user(u -> u.text("""
                Ticker: {ticker}
                Current Price: {price}
                90-day price history: {history}
                Financials: {financials}
                Provide BUY/SELL/HOLD with reasoning.
                """)
                .param("ticker", ticker)
                .param("price", quote.toString())
                .param("history", prices.toString())
                .param("financials", financials.toString()))
            .call()
            .entity(TradingAdvice.class);
    }
}
```

---

## 10. Prompt Engineering in Java

```java
@Component
public class PromptTemplateManager {

    // Load from file
    @Value("classpath:/prompts/trading-analysis.st")
    private Resource tradingPrompt;

    // Parameterized prompts
    public Prompt buildAnalysisPrompt(String ticker, String data) {
        PromptTemplate template = new PromptTemplate(tradingPrompt);
        return template.create(Map.of(
            "ticker", ticker,
            "data", data,
            "date", LocalDate.now().toString()
        ));
    }
}

// System prompt with guardrails
String SYSTEM_PROMPT = """
    You are a licensed financial advisor AI assistant.

    RULES:
    1. Never provide guaranteed returns
    2. Always include risk disclaimers
    3. Base recommendations on provided data only
    4. If data is insufficient, ask for more information
    5. Follow SEC and FINRA compliance guidelines

    OUTPUT FORMAT: Always respond in structured JSON matching the requested schema.
    """;
```

---

## 11. Token Management & Cost Optimization

```java
@Service
public class TokenManagementService {

    private final Counter tokenCounter;
    private final DistributionSummary costTracker;

    public TokenManagementService(MeterRegistry registry) {
        this.tokenCounter = Counter.builder("ai.tokens.total")
            .tag("type", "all").register(registry);
        this.costTracker = DistributionSummary.builder("ai.cost.usd")
            .register(registry);
    }

    // Middleware to track usage
    public ChatResponse trackAndCall(ChatClient client, Prompt prompt) {
        ChatResponse response = client.prompt(prompt).call().chatResponse();

        Usage usage = response.getMetadata().getUsage();
        long totalTokens = usage.getTotalTokens();

        tokenCounter.increment(totalTokens);
        double cost = calculateCost(usage);
        costTracker.record(cost);

        log.info("Tokens used: {} (prompt: {}, completion: {}), Cost: ${}",
            totalTokens, usage.getPromptTokens(),
            usage.getCompletionTokens(), cost);

        return response;
    }

    // Cost optimization strategies
    // 1. Use cheaper models for simple tasks
    // 2. Cache frequent queries
    // 3. Truncate context when too long
    // 4. Use streaming for better UX without more cost
    // 5. Batch similar requests
}
```

---

## 12. Production Deployment Checklist

| Concern | Solution |
|---------|----------|
| API key security | Vault / AWS Secrets Manager, never in code |
| Rate limiting | Resilience4j `@RateLimiter` on AI endpoints |
| Cost control | Token budgets, model routing (cheap → expensive) |
| Latency | Streaming, async, caching frequent queries |
| Monitoring | Micrometer metrics for tokens, cost, latency |
| Fallback | Circuit breaker with fallback to simpler model |
| Content safety | Input/output filtering, PII redaction |
| Testing | Mock AI responses in tests, golden file testing |
| Scalability | Queue-based processing for heavy AI tasks |

```java
// Rate limiting AI calls
@CircuitBreaker(name = "aiService", fallbackMethod = "aiFallback")
@RateLimiter(name = "aiService")
@Retry(name = "aiService")
public String callAI(String prompt) {
    return chatClient.prompt().user(prompt).call().content();
}

public String aiFallback(String prompt, Throwable t) {
    log.warn("AI service unavailable, using cached/fallback response", t);
    return cachedResponseService.getBestMatch(prompt);
}
```

---

*Next: `springboot-cloud.md` →*
