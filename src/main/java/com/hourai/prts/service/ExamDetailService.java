package com.hourai.prts.service;

import com.hourai.prts.dao.ExamDetailDao;
import com.hourai.prts.entity.ExamDetail;
import java.sql.SQLException;
import java.util.List;

public class ExamDetailService {
    private final ExamDetailDao examDetailDao = new ExamDetailDao();

    public int addExamDetail(ExamDetail ed) throws SQLException {
        return examDetailDao.insert(ed);
    }

    public ExamDetail getExamDetailById(Long id) throws SQLException {
        return examDetailDao.selectById(id);
    }

    public List<ExamDetail> getAllExamDetails() throws SQLException {
        return examDetailDao.selectAll();
    }

    public int updateExamDetail(ExamDetail ed) throws SQLException {
        return examDetailDao.update(ed);
    }

    public int deleteExamDetail(Long id) throws SQLException {
        return examDetailDao.delete(id);
    }
}
