package com.finsight.ai.agent;

import com.finsight.ai.rag.RagService;
import com.finsight.market.MarketService;
import com.finsight.market.StockQuote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-Agent Stock Research Service.
 *
 * Simulates a 4-agent pipeline:
 *   Agent 1 (Market Data)    → Fetches current price, volume, technicals
 *   Agent 2 (News Sentiment) → Analyzes recent market sentiment
 *   Agent 3 (Fundamental)    → RAG search on financial documents
 *   Agent 4 (Synthesis)      → Combines all data into a research report
 *
 * Each agent step is streamed via SSE (Server-Sent Events).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResearchAgentService {

    private final MarketService marketService;
    private final RagService ragService;
    private final ChatClient.Builder chatClientBuilder;
    private final ResearchReportRepository reportRepository;

    public ResearchReport runResearch(Long userId, String symbol) {
        log.info("Starting multi-agent research for {} by user {}", symbol, userId);
        String sym = symbol.toUpperCase();

        List<AgentStep> steps = new ArrayList<>();

        // ── Agent 1: Market Data Agent ──────────────────────────────────
        AgentStep step1 = runMarketDataAgent(sym);
        steps.add(step1);

        // ── Agent 2: News Sentiment Agent ──────────────────────────────
        AgentStep step2 = runSentimentAgent(sym);
        steps.add(step2);

        // ── Agent 3: Fundamental Analysis Agent (RAG) ──────────────────
        AgentStep step3 = runFundamentalAgent(sym);
        steps.add(step3);

        // ── Agent 4: Risk Assessment Agent ─────────────────────────────
        AgentStep step4 = runRiskAgent(sym, step1.output(), step2.output());
        steps.add(step4);

        // ── Synthesis: Generate final report ───────────────────────────
        String finalReport = synthesize(sym, steps);

        // Save report to DB
        ResearchReport saved = reportRepository.save(ResearchReport.builder()
                .userId(userId)
                .symbol(sym)
                .report(finalReport)
                .build());

        saved.setSteps(steps);
        return saved;
    }

    private AgentStep runMarketDataAgent(String symbol) {
        StockQuote quote = marketService.getQuote(symbol);
        String output = String.format("""
                [Market Data Agent] Analysis for %s (%s):
                - Current Price: ₹%s
                - Day Change: %s%% (%s)
                - Day Range: ₹%s - ₹%s
                - Volume: %s shares
                - Technical Signal: %s
                """,
                quote.companyName(), symbol,
                quote.price(),
                quote.changePercent(), quote.change(),
                quote.dayLow(), quote.dayHigh(),
                String.format("%,d", quote.volume()),
                quote.changePercent().doubleValue() > 0 ? "Bullish momentum" : "Bearish pressure"
        );
        log.debug("Market Data Agent completed for {}", symbol);
        return new AgentStep("Market Data Agent", "Fetching real-time price and technical data", output);
    }

    private AgentStep runSentimentAgent(String symbol) {
        ChatClient chatClient = chatClientBuilder.build();
        String prompt = String.format("""
                You are a financial news sentiment analyst. Generate a realistic news sentiment analysis
                for %s stock based on current Indian market conditions. Include:
                1. Overall sentiment score (-1 to +1)
                2. Key themes in recent news
                3. Market buzz factors
                Keep it to 4-5 lines, professional tone.
                """, symbol);

        String output = "[News Sentiment Agent] " + chatClient.prompt()
                .user(prompt).call().content();

        return new AgentStep("News Sentiment Agent", "Analyzing market news and sentiment", output);
    }

    private AgentStep runFundamentalAgent(String symbol) {
        String query = "financial analysis fundamentals revenue profit growth " + symbol;
        List<String> ragResults = ragService.semanticSearch(query, 3);

        StringBuilder output = new StringBuilder("[Fundamental Analysis Agent] RAG Search Results:\n");
        if (ragResults.isEmpty()) {
            output.append("No specific fundamental data found in knowledge base. Using market knowledge.\n");
        } else {
            ragResults.forEach(r -> output.append("• ").append(r, 0, Math.min(r.length(), 200)).append("...\n"));
        }
        return new AgentStep("Fundamental Analysis Agent", "Searching financial knowledge base (RAG)", output.toString());
    }

    private AgentStep runRiskAgent(String symbol, String marketData, String sentiment) {
        ChatClient chatClient = chatClientBuilder.build();
        String prompt = String.format("""
                You are a financial risk analyst. Based on this data for %s:
                
                Market Data: %s
                Sentiment: %s
                
                Provide a brief risk assessment (3-4 lines) covering:
                1. Risk level (Low/Medium/High)
                2. Key risk factors
                3. Suggested position size (conservative/moderate/aggressive investor)
                Add disclaimer: "Not financial advice."
                """, symbol, marketData.substring(0, Math.min(200, marketData.length())),
                sentiment.substring(0, Math.min(200, sentiment.length())));

        String output = "[Risk Assessment Agent] " + chatClient.prompt()
                .user(prompt).call().content();

        return new AgentStep("Risk Assessment Agent", "Evaluating portfolio risk factors", output);
    }

    private String synthesize(String symbol, List<AgentStep> steps) {
        ChatClient chatClient = chatClientBuilder.build();

        String allAgentOutputs = steps.stream()
                .map(AgentStep::output)
                .reduce("", (a, b) -> a + "\n\n" + b);

        String prompt = String.format("""
                You are a senior financial analyst. Based on the following multi-agent research
                outputs for %s, write a comprehensive investment research report.
                
                AGENT OUTPUTS:
                %s
                
                Write a professional report with these sections:
                ## Executive Summary
                ## Price & Technical Analysis
                ## Fundamental Outlook
                ## Risk Assessment
                ## Analyst View
                ## Disclaimer
                
                Keep it factual, professional, and balanced. No direct buy/sell recommendation.
                """, symbol, allAgentOutputs);

        return chatClient.prompt()
                .system("You are a professional equity research analyst. Write in formal financial report style.")
                .user(prompt)
                .call()
                .content();
    }
}
