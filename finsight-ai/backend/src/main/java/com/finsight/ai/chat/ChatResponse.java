package com.finsight.ai.chat;

public record ChatResponse(String sessionId, String question, String answer, int messageCount) {}
