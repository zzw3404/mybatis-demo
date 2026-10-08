package com.example.chapter08;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.chapter08.config.MybatisPlusSessionFactory;
import com.example.chapter08.entity.Student;
import com.example.chapter08.mapper.StudentMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Integration exercises for wrappers, pagination, auto-fill and concurrency controls. */
public class StudentAdvancedMapperTest {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws Exception {
        sqlSessionFactory = MybatisPlusSessionFactory.build();
    }

    @Test
    public void testQueryWrappersAndSelectByMap() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            insertStudent(mapper, "Wrapper A", "wrap-a@test.invalid", "Computer", 92.0, 20);
            insertStudent(mapper, "Wrapper B", "wrap-b@test.invalid", "Computer", 75.0, 22);
            insertStudent(mapper, "Lambda Li", "wrap-li@test.invalid", "Software", 88.0, 19);

            List<Student> nested = mapper.selectList(new QueryWrapper<Student>()
                    .eq("major", "Computer")
                    .and(condition -> condition.gt("score", 90).or().gt("age", 21))
                    .orderByDesc("score"));
            assertEquals(2, nested.size());
            assertEquals("Wrapper A", nested.get(0).getName());

            List<Student> lambdaResults = mapper.selectList(new LambdaQueryWrapper<Student>()
                    .eq(Student::getMajor, "Software")
                    .ge(Student::getScore, 80)
                    .likeRight(Student::getName, "Lambda"));
            assertEquals(1, lambdaResults.size());
            assertEquals("Lambda Li", lambdaResults.get(0).getName());

            Map<String, Object> filters = new HashMap<>();
            filters.put("major", "Software");
            filters.put("age", 19);
            assertEquals(1, mapper.selectByMap(filters).size());
            assertEquals(Long.valueOf(2), mapper.selectCount(
                    new QueryWrapper<Student>().eq("major", "Computer")));
        }
    }

    @Test
    public void testProjectionMapsAndGroupedAggregate() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            insertStudent(mapper, "Projection A", "proj-a@test.invalid", "Software", 80.0, 20);
            insertStudent(mapper, "Projection B", "proj-b@test.invalid", "Software", 90.0, 21);

            List<Student> projected = mapper.selectList(new LambdaQueryWrapper<Student>()
                    .select(Student::getId, Student::getName, Student::getScore)
                    .eq(Student::getMajor, "Software"));
            assertEquals(2, projected.size());
            assertNotNull(projected.get(0).getId());
            assertNull(projected.get(0).getMajor());

            List<Map<String, Object>> maps = mapper.selectMaps(new QueryWrapper<Student>()
                    .select("major", "AVG(score) AS avg_score", "COUNT(*) AS student_count")
                    .eq("major", "Software")
                    .groupBy("major"));
            assertEquals(1, maps.size());
            assertTrue(maps.get(0).containsValue("Software"));
            assertTrue(maps.get(0).containsValue(2L) || maps.get(0).containsValue(2));
        }
    }

    @Test
    public void testConditionalUpdateAndNullFieldStrategy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            insertStudent(mapper, "Strategy A", "strategy-a@test.invalid", "AI", 72.0, 20);

            Student original = mapper.selectList(new QueryWrapper<Student>()
                    .eq("email", "strategy-a@test.invalid")).get(0);
            Student update = new Student();
            update.setName("Strategy A Updated");
            assertEquals(1, mapper.update(update, new LambdaUpdateWrapper<Student>()
                    .eq(Student::getId, original.getId())
                    .lt(Student::getScore, 80)
                    .set(Student::getScore, 79.0)));
            Student afterWrapperUpdate = mapper.selectById(original.getId());
            assertEquals("Strategy A Updated", afterWrapperUpdate.getName());
            assertEquals(Double.valueOf(79.0), afterWrapperUpdate.getScore());

            afterWrapperUpdate.setScore(null);
            afterWrapperUpdate.setName("Null Strategy Verified");
            assertEquals(1, mapper.updateById(afterWrapperUpdate));
            Student afterNullUpdate = mapper.selectById(original.getId());
            assertEquals("Null Strategy Verified", afterNullUpdate.getName());
            assertEquals(Double.valueOf(79.0), afterNullUpdate.getScore());
        }
    }

    @Test
    public void testPageTotalAndRecords() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            insertStudent(mapper, "Page A", "page-a@test.invalid", "Paging", 70.0, 18);
            insertStudent(mapper, "Page B", "page-b@test.invalid", "Paging", 80.0, 19);
            insertStudent(mapper, "Page C", "page-c@test.invalid", "Paging", 90.0, 20);

            IPage<Student> page = mapper.selectPage(new Page<Student>(2, 2),
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getMajor, "Paging")
                            .orderByAsc(Student::getAge));
            assertEquals(3L, page.getTotal());
            assertEquals(2L, page.getPages());
            assertEquals(2L, page.getCurrent());
            assertEquals(1, page.getRecords().size());
            assertEquals("Page C", page.getRecords().get(0).getName());
        }
    }

    @Test
    public void testAutoFillLogicalDeleteAndOptimisticLock() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            Student student = new Student("Lock Test", 20, "lock-test@test.invalid", "Concurrency", 60.0);
            assertEquals(1, mapper.insert(student));
            assertNotNull(student.getCreateTime());
            assertNotNull(student.getUpdateTime());
            student = mapper.selectById(student.getId());
            assertNotNull(student);
            assertEquals(Integer.valueOf(0), student.getVersion());

            Student firstRead = mapper.selectById(student.getId());
            // Clear MyBatis' first-level cache so the second read models another client's stale copy.
            session.clearCache();
            Student staleRead = mapper.selectById(student.getId());
            assertEquals(Integer.valueOf(0), firstRead.getVersion());
            firstRead.setScore(81.0);
            assertEquals(1, mapper.updateById(firstRead));
            assertEquals(Integer.valueOf(1), firstRead.getVersion());

            staleRead.setScore(99.0);
            assertEquals("Stale copy must not overwrite the newer row", 0, mapper.updateById(staleRead));
            Student updated = mapper.selectById(student.getId());
            assertEquals(Double.valueOf(81.0), updated.getScore());
            assertEquals(Integer.valueOf(1), updated.getVersion());
            assertFalse(updated.getUpdateTime().isBefore(updated.getCreateTime()));

            assertEquals(1, mapper.deleteById(student.getId()));
            assertNull(mapper.selectById(student.getId()));
        }
    }

    @Test
    public void testPessimisticLockBlocksAnotherTransactionUntilCommit() throws Exception {
        Long studentId = createCommittedLockFixture();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (SqlSession lockingSession = sqlSessionFactory.openSession(false)) {
            StudentMapper lockingMapper = lockingSession.getMapper(StudentMapper.class);
            assertNotNull(lockingMapper.selectByIdForUpdate(studentId));

            CountDownLatch secondTransactionStarted = new CountDownLatch(1);
            Future<Student> waitingRead = executor.submit(() -> {
                try (SqlSession secondSession = sqlSessionFactory.openSession(false)) {
                    secondTransactionStarted.countDown();
                    return secondSession.getMapper(StudentMapper.class).selectByIdForUpdate(studentId);
                }
            });

            assertTrue("Second transaction did not start", secondTransactionStarted.await(5, TimeUnit.SECONDS));
            try {
                waitingRead.get(1, TimeUnit.SECONDS);
                throw new AssertionError("SELECT FOR UPDATE should wait while the first transaction holds the row lock");
            } catch (TimeoutException expected) {
                // The second SELECT is blocked by InnoDB until the first transaction releases its lock.
            }

            // SELECT is not marked as a dirty SqlSession operation; force the JDBC commit
            // so the FOR UPDATE row lock is released before awaiting the second transaction.
            lockingSession.commit(true);
            assertNotNull(waitingRead.get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
            cleanupLockFixture(studentId);
        }
    }

    private Long createCommittedLockFixture() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Student student = new Student("Pessimistic Lock", 22,
                    "pessimistic-lock@test.invalid", "Concurrency", 70.0);
            session.getMapper(StudentMapper.class).insert(student);
            session.commit();
            return student.getId();
        }
    }

    private void cleanupLockFixture(Long id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            session.getMapper(StudentMapper.class).deleteById(id);
            session.commit();
        }
    }

    private static void insertStudent(StudentMapper mapper, String name, String email,
                                      String major, double score, int age) {
        assertEquals(1, mapper.insert(new Student(name, age, email, major, score)));
    }
}
