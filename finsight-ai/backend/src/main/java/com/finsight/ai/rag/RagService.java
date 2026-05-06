package com.finsight.ai.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval-Augmented Generation) Service.
 *
 * Flow:
 *   1. Embed user query using OpenAI text-embedding-3-small
 *   2. Cosine similarity search in pgvector → top-K relevant chunks
 *   3. Assemble prompt: system instructions + retrieved context + user question
 *   4. Send to GPT-4o-mini → grounded, accurate response
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;

    @Value("${app.ai.max-context-tokens:3000}")
    private int maxContextTokens;

    private static final String SYSTEM_PROMPT = """
            You are FinSight AI, a professional financial analyst assistant for Indian markets.
            You provide clear, accurate, and professional financial analysis.
            
            RULES:
            - Answer ONLY based on the provided context below. Do not use external knowledge.
            - If the context does not contain the answer, say "I don't have enough information to answer that from the available documents."
            - Never provide direct buy/sell recommendations. Always add a disclaimer.
            - Keep responses concise (max 300 words) unless a detailed analysis is explicitly requested.
            - Format numbers in Indian number system (lakhs, crores) when relevant.
            """;

    /**
     * Perform a RAG query: retrieve relevant context from vector store, then call LLM.
     */
    public String query(String userQuestion, String sessionContext) {
        log.debug("RAG query: {}", userQuestion);

        // Step 1: Search vector store for relevant chunks
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuestion)
                        .topK(5)
                        .similarityThreshold(0.6)
                        .build()
        );

        if (relevantDocs.isEmpty()) {
            return callLlmWithNoContext(userQuestion);
        }

        // Step 2: Assemble context (respect token budget)
        String context = buildContext(relevantDocs);

        // Step 3: Build full prompt and call LLM
        String userPrompt = String.format("""
                CONTEXT (from financial documents):
                ---
                %s
                ---
                
                %sCONVERSATION HISTORY:
                %s
                
                USER QUESTION: %s
                """, context,
                sessionContext != null ? "RECENT " : "",
                sessionContext != null ? sessionContext : "(No previous context)",
                userQuestion);

        log.debug("Calling LLM with {} context chunks", relevantDocs.size());

        ChatClient chatClient = chatClientBuilder.build();
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();
    }

    /**
     * Ingest a text document into the vector store (chunk + embed + store).
     */
    public int ingestDocument(String content, String source, String docType) {
        List<String> chunks = chunkText(content, 800, 100);
        List<Document> documents = chunks.stream()
                .map(chunk -> new Document(chunk,
                        java.util.Map.of("source", source, "doc_type", docType)))
                .collect(Collectors.toList());

        vectorStore.add(documents);
        log.info("Ingested {} chunks from document: {}", documents.size(), source);
        return documents.size();
    }

    /**
     * Simple semantic search — return top-K matching document excerpts.
     */
    public List<String> semanticSearch(String query, int topK) {
        return vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(topK).build()
        ).stream().map(Document::getText).toList();
    }

    // ──────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────

    private String callLlmWithNoContext(String question) {
        ChatClient chatClient = chatClientBuilder.build();
        return chatClient.prompt()
                .system(SYSTEM_PROMPT + "\nNo context documents are available for this query.")
                .user(question)
                .call()
                .content();
    }

    private String buildContext(List<Document> docs) {
        StringBuilder sb = new StringBuilder();
        int estimatedTokens = 0;
        for (Document doc : docs) {
            String text = doc.getText();
            int tokens = text.length() / 4; // rough estimate: 1 token ≈ 4 chars
            if (estimatedTokens + tokens > maxContextTokens) break;
            sb.append(text).append("\n\n---\n\n");
            estimatedTokens += tokens;
        }
        return sb.toString();
    }

    /**
     * Split text into overlapping chunks for better retrieval coverage.
     */
    private List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new java.util.ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {
            if (current.length() + sentence.length() > chunkSize) {
                if (!current.isEmpty()) {
                    chunks.add(current.toString().trim());
                    // Overlap: keep last portion
                    String overlap_text = current.length() > overlap
                            ? current.substring(current.length() - overlap)
                            : current.toString();
                    current = new StringBuilder(overlap_text);
                }
            }
            current.append(sentence).append(" ");
        }
        if (!current.isEmpty()) chunks.add(current.toString().trim());
        return chunks;
    }
}
