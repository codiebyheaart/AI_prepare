package com.finsight.portfolio;

import com.finsight.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.holdings WHERE p.user = :user")
    Optional<Portfolio> findByUser(User user);
}
