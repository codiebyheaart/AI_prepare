package com.finsight.market;

import java.math.BigDecimal;

public record StockQuote(
        String symbol,
        String companyName,
        BigDecimal price,
        BigDecimal previousClose,
        BigDecimal change,
        BigDecimal changePercent,
        BigDecimal dayLow,
        BigDecimal dayHigh,
        Long volume
) {}
