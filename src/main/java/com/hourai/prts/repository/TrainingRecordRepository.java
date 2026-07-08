package com.hourai.prts.repository;

import com.hourai.prts.entity.TrainingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, TrainingRecord.TrainingRecordId> {
    List<TrainingRecord> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TrainingRecord t WHERE t.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
