package com.finsight.ai.document;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_documents")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UploadedDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id") private Long userId;
    @Column(name = "file_name") private String fileName;
    @Column(name = "doc_type") private String docType;
    @Column(name = "file_size") private Long fileSize;
    @Column(name = "chunk_count") private Integer chunkCount;
    private String status;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
