package com.finsight.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByPortfolioOrderByTimestampDesc(Portfolio portfolio);
}
