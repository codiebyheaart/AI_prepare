package com.finsight.ai.document;

import com.finsight.ai.rag.RagService;
import com.finsight.auth.User;
import com.finsight.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documents", description = "Upload documents to power the RAG knowledge base")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;
    private final RagService ragService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document (PDF/TXT) to the RAG knowledge base")
    public ResponseEntity<ApiResponse<UploadedDocumentDto>> upload(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "docType", defaultValue = "GENERAL") String docType) {

        UploadedDocumentDto result = documentService.processUpload(user, file, docType);
        return ResponseEntity.ok(ApiResponse.success(
                "Document uploaded and indexed (" + result.chunkCount() + " chunks)", result));
    }

    @GetMapping
    @Operation(summary = "List all uploaded documents")
    public ResponseEntity<ApiResponse<List<UploadedDocumentDto>>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Documents loaded", documentService.listDocuments(user)));
    }

    @GetMapping("/semantic-search")
    @Operation(summary = "Semantic search across all indexed documents")
    public ResponseEntity<ApiResponse<List<String>>> semanticSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int topK) {
        List<String> results = ragService.semanticSearch(q, topK);
        return ResponseEntity.ok(ApiResponse.success("Search results (" + results.size() + " found)", results));
    }
}
