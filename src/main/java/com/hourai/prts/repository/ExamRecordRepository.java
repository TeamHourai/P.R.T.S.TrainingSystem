package com.hourai.prts.repository;

import com.hourai.prts.entity.ExamRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRecordRepository extends JpaRepository<ExamRecord, Long> {
    Page<ExamRecord> findByUserId(Long userId, Pageable pageable);
    List<ExamRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}
