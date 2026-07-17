package com.hourai.prts.repository;

import com.hourai.prts.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** 按时间倒序分页查询，供超级管理员审计。 */
    Page<AuditLog> findAllByOrderByIdDesc(Pageable pageable);
}
