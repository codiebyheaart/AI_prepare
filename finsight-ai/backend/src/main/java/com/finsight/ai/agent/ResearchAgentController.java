package com.finsight.ai.agent;

import com.finsight.auth.User;
import com.finsight.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/research")
@RequiredArgsConstructor
@Tag(name = "AI Research Agent", description = "Multi-agent stock research and analysis")
@SecurityRequirement(name = "bearerAuth")
public class ResearchAgentController {

    private final ResearchAgentService researchAgentService;
    private final ResearchReportRepository reportRepository;

    @PostMapping("/{symbol}")
    @Operation(summary = "Run multi-agent research on a stock (takes 10-20 seconds)")
    public ResponseEntity<ApiResponse<ResearchReport>> runResearch(
            @AuthenticationPrincipal User user,
            @PathVariable String symbol) {

        ResearchReport report = researchAgentService.runResearch(user.getId(), symbol);
        return ResponseEntity.ok(ApiResponse.success("Research complete", report));
    }

    @GetMapping("/history")
    @Operation(summary = "Get past research reports for the current user")
    public ResponseEntity<ApiResponse<List<ResearchReport>>> getHistory(
            @AuthenticationPrincipal User user) {
        List<ResearchReport> reports = reportRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Reports loaded", reports));
    }
}
