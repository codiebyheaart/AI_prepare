package com.finsight.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulates realistic stock prices using a random-walk algorithm.
 * Prices are seeded with real NSE baseline values and fluctuate ±2% per minute.
 */
@Service
@Slf4j
public class MarketService {

    // Baseline prices (approximate NSE values as of early 2026)
    private static final Map<String, Double> BASELINE = Map.ofEntries(
            Map.entry("RELIANCE",   2870.0),
            Map.entry("TCS",        4120.0),
            Map.entry("INFY",       1850.0),
            Map.entry("HDFCBANK",   1845.0),
            Map.entry("ICICIBANK",  1320.0),
            Map.entry("SBIN",        830.0),
            Map.entry("BAJFINANCE", 7200.0),
            Map.entry("WIPRO",       580.0),
            Map.entry("ASIANPAINT", 2900.0),
            Map.entry("MARUTI",    12400.0),
            Map.entry("TATAMOTORS",  975.0),
            Map.entry("SUNPHARMA", 1920.0),
            Map.entry("DRREDDY",   6750.0),
            Map.entry("ONGC",       270.0),
            Map.entry("LTIM",      5800.0),
            Map.entry("ADANIENT",  2650.0),
            Map.entry("HINDUNILVR",2450.0),
            Map.entry("KOTAKBANK", 1920.0),
            Map.entry("TITAN",     3850.0),
            Map.entry("POWERGRID",  320.0),
            Map.entry("NIFTY50",  22800.0),
            Map.entry("SENSEX",   75200.0),
            Map.entry("BANKNIFTY",49500.0)
    );

    private final Map<String, Double> prices = new ConcurrentHashMap<>(BASELINE);
    private final Random rng = new Random();

    /** Simulate tick updates every 10 seconds */
    @Scheduled(fixedDelay = 10_000)
    public void simulateTick() {
        prices.replaceAll((symbol, price) -> {
            double changePercent = (rng.nextGaussian() * 0.005); // ~0.5% std dev per tick
            return Math.max(price * (1 + changePercent), 1.0);
        });
    }

    public BigDecimal getPrice(String symbol) {
        Double price = prices.getOrDefault(symbol.toUpperCase(),
                BASELINE.getOrDefault(symbol.toUpperCase(), 1000.0));
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    public StockQuote getQuote(String symbol) {
        String sym = symbol.toUpperCase();
        double current   = prices.getOrDefault(sym, BASELINE.getOrDefault(sym, 1000.0));
        double prev      = BASELINE.getOrDefault(sym, current);
        double change    = current - prev;
        double changePct = prev == 0 ? 0 : (change / prev) * 100;

        return new StockQuote(sym, getCompanyName(sym),
                round(current), round(prev),
                round(change), round(changePct),
                round(current * 0.97),   // simulated low
                round(current * 1.03),   // simulated high
                (long) (rng.nextInt(5_000_000) + 500_000));  // simulated volume
    }

    public List<StockQuote> getTrending() {
        List<String> trending = List.of("RELIANCE","TCS","HDFCBANK","ICICIBANK","INFY",
                "SBIN","BAJFINANCE","MARUTI","TATAMOTORS","TITAN");
        return trending.stream().map(this::getQuote).toList();
    }

    public List<StockQuote> searchStocks(String query) {
        if (query == null || query.isBlank()) return getTrending();
        String q = query.toUpperCase();
        return BASELINE.keySet().stream()
                .filter(sym -> sym.contains(q) || getCompanyName(sym).toUpperCase().contains(q))
                .map(this::getQuote)
                .limit(10)
                .toList();
    }

    public Map<String, StockQuote> getIndices() {
        return Map.of(
                "NIFTY50",   getQuote("NIFTY50"),
                "SENSEX",    getQuote("SENSEX"),
                "BANKNIFTY", getQuote("BANKNIFTY")
        );
    }

    private BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private String getCompanyName(String symbol) {
        Map<String, String> names = Map.ofEntries(
                Map.entry("RELIANCE",   "Reliance Industries"),
                Map.entry("TCS",        "Tata Consultancy Services"),
                Map.entry("INFY",       "Infosys"),
                Map.entry("HDFCBANK",   "HDFC Bank"),
                Map.entry("ICICIBANK",  "ICICI Bank"),
                Map.entry("SBIN",       "State Bank of India"),
                Map.entry("BAJFINANCE", "Bajaj Finance"),
                Map.entry("WIPRO",      "Wipro"),
                Map.entry("ASIANPAINT", "Asian Paints"),
                Map.entry("MARUTI",     "Maruti Suzuki"),
                Map.entry("TATAMOTORS", "Tata Motors"),
                Map.entry("SUNPHARMA",  "Sun Pharma"),
                Map.entry("DRREDDY",    "Dr Reddy's Labs"),
                Map.entry("ONGC",       "ONGC"),
                Map.entry("LTIM",       "LTIMindtree"),
                Map.entry("ADANIENT",   "Adani Enterprises"),
                Map.entry("HINDUNILVR", "Hindustan Unilever"),
                Map.entry("KOTAKBANK",  "Kotak Mahindra Bank"),
                Map.entry("TITAN",      "Titan Company"),
                Map.entry("POWERGRID",  "Power Grid Corp"),
                Map.entry("NIFTY50",    "Nifty 50 Index"),
                Map.entry("SENSEX",     "BSE Sensex"),
                Map.entry("BANKNIFTY",  "Nifty Bank Index")
        );
        return names.getOrDefault(symbol, symbol);
    }
}
