package com.hourai.prts.dao;

import com.hourai.prts.entity.Question;
import org.junit.jupiter.api.*;
import java.sql.SQLException;

public class QuestionDaoTest {
    private QuestionDao questionDao;

    @BeforeEach
    public void setUp() {
        questionDao = new QuestionDao();
    }

    @Test
    public void testInsertAndSelectById() throws SQLException {
        Question q = new Question();
        // TODO: 设置 q 的各项属性，确保插入数据完整
        q.setType(1);
        q.setDifficulty(2);
        q.setCategory("测试类别");
        q.setResource("测试资源");
        q.setQuestion("测试题目");
        q.setOptions("A;B;C;D");
        q.setAnswer("A");
        q.setAnalysis("测试解析");
        q.setHasPicture(false);
        q.setPictureUrl("");
        q.setViewCount(0);
        q.setErrorCount(0);
        int result = questionDao.insert(q);
        Assertions.assertTrue(result > 0);
        // 假设插入后能获取自增主键 id
        // long id = ...;
        // Question selected = questionDao.selectById(id);
        // Assertions.assertNotNull(selected);
        // Assertions.assertEquals(q.getQuestion(), selected.getQuestion());
    }

    @Test
    public void testUpdate() throws SQLException {
        // 先插入一条数据
        Question q = new Question();
        q.setType(1);
        q.setDifficulty(2);
        q.setCategory("update类别");
        q.setResource("update资源");
        q.setQuestion("update题目");
        q.setOptions("A;B;C;D");
        q.setAnswer("B");
        q.setAnalysis("update解析");
        q.setHasPicture(false);
        q.setPictureUrl("");
        q.setViewCount(0);
        q.setErrorCount(0);
        int result = questionDao.insert(q);
        Assertions.assertTrue(result > 0);
        // 查询插入后的 id
        Question inserted = questionDao.selectAll().stream().filter(x -> "update题目".equals(x.getQuestion())).findFirst().orElse(null);
        Assertions.assertNotNull(inserted);
        inserted.setQuestion("update题目2");
        int updateResult = questionDao.update(inserted);
        Assertions.assertTrue(updateResult > 0);
        Question updated = questionDao.selectById(inserted.getId());
        Assertions.assertEquals("update题目2", updated.getQuestion());
    }

    @Test
    public void testDelete() throws SQLException {
        // 先插入一条数据
        Question q = new Question();
        q.setType(1);
        q.setDifficulty(2);
        q.setCategory("delete类别");
        q.setResource("delete资源");
        q.setQuestion("delete题目");
        q.setOptions("A;B;C;D");
        q.setAnswer("C");
        q.setAnalysis("delete解析");
        q.setHasPicture(false);
        q.setPictureUrl("");
        q.setViewCount(0);
        q.setErrorCount(0);
        int result = questionDao.insert(q);
        Assertions.assertTrue(result > 0);
        // 查询插入后的 id
        Question inserted = questionDao.selectAll().stream().filter(x -> "delete题目".equals(x.getQuestion())).findFirst().orElse(null);
        Assertions.assertNotNull(inserted);
        int deleteResult = questionDao.delete(inserted.getId());
        Assertions.assertTrue(deleteResult > 0);
        Question deleted = questionDao.selectById(inserted.getId());
        Assertions.assertNull(deleted);
    }

    @Test
    public void testSelectAll() throws SQLException {
        // 只需保证 selectAll 能正常返回列表
        Assertions.assertNotNull(questionDao.selectAll());
    }
}
