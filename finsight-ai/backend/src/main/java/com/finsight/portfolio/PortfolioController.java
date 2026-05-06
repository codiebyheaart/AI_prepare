package com.finsight.portfolio;

import com.finsight.auth.User;
import com.finsight.common.ApiResponse;
import com.finsight.market.MarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Manage stock portfolio and holdings")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    @Operation(summary = "Get user's portfolio with live P&L")
    public ResponseEntity<ApiResponse<PortfolioDto>> getPortfolio(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Portfolio loaded", portfolioService.getPortfolio(user)));
    }

    @PostMapping("/holdings")
    @Operation(summary = "Add a stock holding to the portfolio")
    public ResponseEntity<ApiResponse<HoldingDto>> addHolding(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddHoldingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Holding added", portfolioService.addHolding(user, request)));
    }

    @DeleteMapping("/holdings/{holdingId}")
    @Operation(summary = "Remove a holding from the portfolio")
    public ResponseEntity<ApiResponse<Void>> removeHolding(
            @AuthenticationPrincipal User user,
            @PathVariable Long holdingId) {
        portfolioService.removeHolding(user, holdingId);
        return ResponseEntity.ok(ApiResponse.success("Holding removed", null));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get transaction history")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getTransactions(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Transactions loaded", portfolioService.getTransactions(user)));
    }
}
