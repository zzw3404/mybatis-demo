package com.example.chapter08;

import com.example.chapter08.config.MybatisPlusSessionFactory;
import com.example.chapter08.entity.Student;
import com.example.chapter08.mapper.StudentMapper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Smoke tests for MyBatis-Plus BaseMapper CRUD. Each test rolls back its database writes. */
public class StudentBaseMapperTest {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws Exception {
        sqlSessionFactory = MybatisPlusSessionFactory.build();
    }

    @Test
    public void testMybatisPlusGlobalConfigIsRegistered() {
        assertTrue(GlobalConfigUtils.getMetaObjectHandler(sqlSessionFactory.getConfiguration()).isPresent());
    }

    @Test
    public void testInsertSelectUpdateAndLogicalDeleteById() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            Student student = new Student("MP CRUD Test", 20, "mp-crud@test.invalid", "Computer", 85.0);

            assertEquals(1, mapper.insert(student));
            assertNotNull(student.getId());
            assertNotNull(student.getCreateTime());
            assertNotNull(student.getUpdateTime());

            Student found = mapper.selectById(student.getId());
            assertNotNull(found);
            assertEquals("MP CRUD Test", found.getName());

            found.setScore(91.0);
            assertEquals(1, mapper.updateById(found));
            assertEquals(Double.valueOf(91.0), mapper.selectById(found.getId()).getScore());

            assertEquals(1, mapper.deleteById(found.getId()));
            assertNull(mapper.selectById(found.getId()));
        }
    }

    @Test
    public void testBatchSelectAndDeleteByIds() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            Student first = new Student("MP Batch A", 19, "mp-batch-a@test.invalid", "Software", 80.0);
            Student second = new Student("MP Batch B", 20, "mp-batch-b@test.invalid", "Software", 82.0);
            Student third = new Student("MP Batch C", 21, "mp-batch-c@test.invalid", "Software", 84.0);

            assertEquals(1, mapper.insert(first));
            assertEquals(1, mapper.insert(second));
            assertEquals(1, mapper.insert(third));
            List<Long> ids = Arrays.asList(first.getId(), second.getId(), third.getId());

            assertEquals(3, mapper.selectBatchIds(ids).size());
            assertEquals(3, mapper.deleteBatchIds(ids));
            assertTrue(mapper.selectBatchIds(ids).isEmpty());
        }
    }
}
