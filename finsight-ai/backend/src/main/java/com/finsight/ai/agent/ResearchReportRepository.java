package com.finsight.ai.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResearchReportRepository extends JpaRepository<ResearchReport, Long> {
    List<ResearchReport> findByUserIdOrderByCreatedAtDesc(Long userId);
}
