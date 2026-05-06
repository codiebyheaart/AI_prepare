package com.finsight.portfolio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDto(
        Long id, String symbol, String companyName,
        String type, BigDecimal quantity,
        BigDecimal price, BigDecimal totalAmount,
        LocalDateTime timestamp
) {
    public static TransactionDto from(Transaction t) {
        return new TransactionDto(t.getId(), t.getSymbol(), t.getCompanyName(),
                t.getType(), t.getQuantity(), t.getPrice(), t.getTotalAmount(), t.getTimestamp());
    }
}
