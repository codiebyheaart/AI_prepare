package com.finsight.ai.agent;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "research_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(columnDefinition = "TEXT")
    private String report;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Transient — not persisted, only in-memory for response
    @Transient
    private List<AgentStep> steps;
}
