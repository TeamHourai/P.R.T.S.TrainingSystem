package com.hourai.prts.repository;

import com.hourai.prts.entity.AnswerSettings;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerSettingsRepository extends CrudRepository<AnswerSettings, Long> {
}
