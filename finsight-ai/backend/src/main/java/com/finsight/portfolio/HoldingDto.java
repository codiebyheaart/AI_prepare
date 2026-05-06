package com.finsight.portfolio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HoldingDto(
        Long id, String symbol, String companyName,
        BigDecimal quantity, BigDecimal avgBuyPrice,
        BigDecimal currentPrice, BigDecimal currentValue,
        BigDecimal pnl, BigDecimal pnlPercent,
        LocalDateTime addedAt
) {}
