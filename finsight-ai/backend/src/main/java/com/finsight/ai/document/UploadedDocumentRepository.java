package com.finsight.ai.document;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, Long> {
    List<UploadedDocument> findByUserIdOrderByCreatedAtDesc(Long userId);
}
