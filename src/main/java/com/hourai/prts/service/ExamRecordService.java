package com.hourai.prts.service;

import com.hourai.prts.dao.ExamRecordDao;
import com.hourai.prts.entity.ExamRecord;
import java.sql.SQLException;
import java.util.List;

public class ExamRecordService {
    private final ExamRecordDao examRecordDao = new ExamRecordDao();

    public int addExamRecord(ExamRecord er) throws SQLException {
        return examRecordDao.insert(er);
    }

    public ExamRecord getExamRecordById(Long id) throws SQLException {
        return examRecordDao.selectById(id);
    }

    public List<ExamRecord> getAllExamRecords() throws SQLException {
        return examRecordDao.selectAll();
    }

    public int updateExamRecord(ExamRecord er) throws SQLException {
        return examRecordDao.update(er);
    }

    public int deleteExamRecord(Long id) throws SQLException {
        return examRecordDao.delete(id);
    }
}
