package com.finsight.market;

import com.finsight.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Stock prices, quotes and search")
@SecurityRequirement(name = "bearerAuth")
public class MarketController {

    private final MarketService marketService;

    @GetMapping("/quote/{symbol}")
    @Operation(summary = "Get current stock quote")
    public ResponseEntity<ApiResponse<StockQuote>> getQuote(@PathVariable String symbol) {
        return ResponseEntity.ok(ApiResponse.success("Quote fetched", marketService.getQuote(symbol)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search stocks by symbol or company name")
    public ResponseEntity<ApiResponse<List<StockQuote>>> search(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.success("Search results", marketService.searchStocks(q)));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get top trending stocks")
    public ResponseEntity<ApiResponse<List<StockQuote>>> trending() {
        return ResponseEntity.ok(ApiResponse.success("Trending stocks", marketService.getTrending()));
    }

    @GetMapping("/indices")
    @Operation(summary = "Get market indices (Nifty, Sensex, Bank Nifty)")
    public ResponseEntity<ApiResponse<Map<String, StockQuote>>> indices() {
        return ResponseEntity.ok(ApiResponse.success("Market indices", marketService.getIndices()));
    }
}
