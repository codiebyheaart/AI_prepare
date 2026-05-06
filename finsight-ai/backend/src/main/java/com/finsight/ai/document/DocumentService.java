package com.finsight.ai.document;

import com.finsight.ai.rag.RagService;
import com.finsight.auth.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final RagService ragService;
    private final UploadedDocumentRepository documentRepository;

    public UploadedDocumentDto processUpload(User user, MultipartFile file, String docType) {
        String fileName = file.getOriginalFilename();
        log.info("Processing document upload: {} for user {}", fileName, user.getEmail());

        // Extract text content
        String content;
        try {
            content = extractText(file);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read file: " + e.getMessage());
        }

        if (content.isBlank()) {
            throw new IllegalArgumentException("Document appears to be empty or unreadable");
        }

        // Save document record
        UploadedDocument doc = UploadedDocument.builder()
                .userId(user.getId())
                .fileName(fileName)
                .docType(docType)
                .fileSize(file.getSize())
                .status("PROCESSING")
                .build();
        doc = documentRepository.save(doc);

        // Ingest into vector store (RAG)
        int chunkCount = ragService.ingestDocument(content, fileName, docType);

        // Update status
        doc.setChunkCount(chunkCount);
        doc.setStatus("INDEXED");
        doc = documentRepository.save(doc);

        log.info("Document {} indexed with {} chunks", fileName, chunkCount);
        return UploadedDocumentDto.from(doc);
    }

    public List<UploadedDocumentDto> listDocuments(User user) {
        return documentRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(UploadedDocumentDto::from).toList();
    }

    private String extractText(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (name.endsWith(".txt") || name.endsWith(".md")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
        // For PDF: read as text (real implementation would use Apache PDFBox)
        // For demo, attempt UTF-8 and filter readable characters
        String raw = new String(file.getBytes(), StandardCharsets.UTF_8);
        return raw.replaceAll("[^\\x20-\\x7E\\n\\r\\t]", " ")
                  .replaceAll("\\s{3,}", "\n")
                  .trim();
    }
}
