package com.finsight.portfolio;

import com.finsight.auth.User;
import com.finsight.market.MarketService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final MarketService marketService;

    @Transactional
    public Portfolio getOrCreatePortfolio(User user) {
        return portfolioRepository.findByUser(user)
                .orElseGet(() -> portfolioRepository.save(
                        Portfolio.builder().user(user).name("My Portfolio").build()));
    }

    public PortfolioDto getPortfolio(User user) {
        Portfolio portfolio = getOrCreatePortfolio(user);
        List<HoldingDto> holdingDtos = portfolio.getHoldings().stream()
                .map(h -> {
                    BigDecimal currentPrice = marketService.getPrice(h.getSymbol());
                    BigDecimal currentValue  = currentPrice.multiply(h.getQuantity());
                    BigDecimal investedValue = h.getAvgBuyPrice().multiply(h.getQuantity());
                    BigDecimal pnl   = currentValue.subtract(investedValue);
                    BigDecimal pnlPct = investedValue.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                            : pnl.divide(investedValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    return new HoldingDto(h.getId(), h.getSymbol(), h.getCompanyName(),
                            h.getQuantity(), h.getAvgBuyPrice(), currentPrice,
                            currentValue, pnl, pnlPct, h.getAddedAt());
                }).toList();

        BigDecimal totalInvested = holdingDtos.stream()
                .map(d -> d.avgBuyPrice().multiply(d.quantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCurrent = holdingDtos.stream()
                .map(HoldingDto::currentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPnl    = totalCurrent.subtract(totalInvested);
        BigDecimal totalPnlPct = totalInvested.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : totalPnl.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return new PortfolioDto(portfolio.getId(), portfolio.getName(),
                holdingDtos, totalInvested, totalCurrent, totalPnl, totalPnlPct);
    }

    @Transactional
    public HoldingDto addHolding(User user, AddHoldingRequest request) {
        Portfolio portfolio = getOrCreatePortfolio(user);
        Holding holding = Holding.builder()
                .portfolio(portfolio)
                .symbol(request.symbol().toUpperCase())
                .companyName(request.companyName())
                .quantity(request.quantity())
                .avgBuyPrice(request.avgBuyPrice())
                .build();
        holding = holdingRepository.save(holding);

        // Record transaction
        Transaction tx = Transaction.builder()
                .portfolio(portfolio)
                .symbol(holding.getSymbol())
                .companyName(holding.getCompanyName())
                .type("BUY")
                .quantity(request.quantity())
                .price(request.avgBuyPrice())
                .totalAmount(request.avgBuyPrice().multiply(request.quantity()))
                .build();
        transactionRepository.save(tx);

        BigDecimal currentPrice = marketService.getPrice(holding.getSymbol());
        BigDecimal cv  = currentPrice.multiply(holding.getQuantity());
        BigDecimal inv = holding.getAvgBuyPrice().multiply(holding.getQuantity());
        BigDecimal pnl = cv.subtract(inv);
        BigDecimal pct = inv.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : pnl.divide(inv, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return new HoldingDto(holding.getId(), holding.getSymbol(), holding.getCompanyName(),
                holding.getQuantity(), holding.getAvgBuyPrice(), currentPrice, cv, pnl, pct, holding.getAddedAt());
    }

    @Transactional
    public void removeHolding(User user, Long holdingId) {
        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new EntityNotFoundException("Holding not found: " + holdingId));
        if (!holding.getPortfolio().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        holdingRepository.delete(holding);
    }

    public List<TransactionDto> getTransactions(User user) {
        Portfolio portfolio = getOrCreatePortfolio(user);
        return transactionRepository.findByPortfolioOrderByTimestampDesc(portfolio)
                .stream().map(TransactionDto::from).toList();
    }
}
