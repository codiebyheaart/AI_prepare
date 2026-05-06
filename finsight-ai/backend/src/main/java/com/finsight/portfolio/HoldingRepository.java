package com.finsight.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByPortfolio(Portfolio portfolio);
}
