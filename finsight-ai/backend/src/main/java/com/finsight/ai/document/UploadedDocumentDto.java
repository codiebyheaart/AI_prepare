package com.finsight.ai.document;

import java.time.LocalDateTime;

public record UploadedDocumentDto(
        Long id, String fileName, String docType,
        Long fileSize, Integer chunkCount, String status,
        LocalDateTime createdAt
) {
    public static UploadedDocumentDto from(UploadedDocument d) {
        return new UploadedDocumentDto(d.getId(), d.getFileName(), d.getDocType(),
                d.getFileSize(), d.getChunkCount(), d.getStatus(), d.getCreatedAt());
    }
}
