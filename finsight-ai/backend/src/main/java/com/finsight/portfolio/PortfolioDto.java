package com.finsight.portfolio;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioDto(
        Long id, String name,
        List<HoldingDto> holdings,
        BigDecimal totalInvested,
        BigDecimal totalCurrentValue,
        BigDecimal totalPnl,
        BigDecimal totalPnlPercent
) {}
