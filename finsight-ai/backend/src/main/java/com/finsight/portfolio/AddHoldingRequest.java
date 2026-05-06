package com.finsight.portfolio;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AddHoldingRequest(
        @NotBlank @Size(max = 20) String symbol,
        String companyName,
        @NotNull @DecimalMin("0.0001") BigDecimal quantity,
        @NotNull @DecimalMin("0.01") BigDecimal avgBuyPrice
) {}
