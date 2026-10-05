package com.example.chapter05;

import com.example.chapter05.entity.Dept;
import com.example.chapter05.entity.Emp;
import com.example.chapter05.entity.Skill;
import com.example.chapter05.mapper.Chapter05DeptMapper;
import com.example.chapter05.mapper.Chapter05EmpMapper;
import com.example.chapter05.mapper.Chapter05SkillMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/** Adapted from Documents/chapter05/chapter05test/Chapter05Test.java. */
public class Chapter05Test {

    private SqlSessionFactory sqlSessionFactory;
    private SqlStatementCounter sqlStatementCounter;

    @Before
    public void init() throws Exception {
        try (InputStream inputStream = Resources.getResourceAsStream("chapter05/mybatis-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        }
        sqlStatementCounter = new SqlStatementCounter();
        sqlSessionFactory.getConfiguration().addInterceptor(sqlStatementCounter);
    }

    @Test
    public void testOne2oneByXml() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Emp emp = session.getMapper(Chapter05EmpMapper.class).one2oneByXml(1);
            assertNotNull(emp);
            assertEquals("SMITH", emp.getEname());
            assertEquals("RESEARCH", emp.getDept().getDname());
        }
    }

    @Test
    public void testOne2oneByXmlNestedSelect() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Emp emp = session.getMapper(Chapter05EmpMapper.class).one2oneByXmlSelect(1);
            assertNotNull(emp);
            assertEquals("SMITH", emp.getEname());
            assertEquals("RESEARCH", emp.getDept().getDname());
        }
    }

    @Test
    public void testOne2oneByAnnotation() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Emp emp = session.getMapper(Chapter05EmpMapper.class).one2oneByAnn(2);
            assertNotNull(emp);
            assertEquals("ALLEN", emp.getEname());
            assertEquals("SALES", emp.getDept().getDname());
        }
    }

    @Test
    public void testMany2oneByXml() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<Emp> emps = session.getMapper(Chapter05EmpMapper.class).many2oneByXml();
            assertEquals(14, emps.size());
            assertEquals("RESEARCH", emps.get(0).getDept().getDname());
        }
    }

    @Test
    public void testMany2oneByXmlNestedSelect() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<Emp> emps = session.getMapper(Chapter05EmpMapper.class).many2oneByXmlSelect();
            assertEquals(14, emps.size());
            assertEquals("SMITH", emps.get(0).getEname());
            assertEquals("RESEARCH", emps.get(0).getDept().getDname());
            assertEquals("ALLEN", emps.get(1).getEname());
            assertEquals("SALES", emps.get(1).getDept().getDname());
        }
    }

    @Test
    public void testMany2oneByAnnotation() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<Emp> emps = session.getMapper(Chapter05EmpMapper.class).many2oneByAnn();
            assertEquals(14, emps.size());
            assertEquals("RESEARCH", emps.get(0).getDept().getDname());
        }
    }

    @Test
    public void testOne2manyByXml() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Dept dept = session.getMapper(Chapter05DeptMapper.class).one2manyByXml(2);
            assertNotNull(dept);
            assertEquals("RESEARCH", dept.getDname());
            assertEquals(5, dept.getEmps().size());
        }
    }

    @Test
    public void testOne2manyByAnnotation() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Dept dept = session.getMapper(Chapter05DeptMapper.class).one2manyByAnn(3);
            assertNotNull(dept);
            assertEquals("SALES", dept.getDname());
            assertEquals(6, dept.getEmps().size());
        }
    }

    @Test
    public void testMany2manyByXml() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Emp emp = session.getMapper(Chapter05EmpMapper.class).many2manyByXml(8);
            assertNotNull(emp);
            assertEquals("SCOTT", emp.getEname());
            assertEquals(3, emp.getSkills().size());
        }
    }

    @Test
    public void testMany2manyByAnnotation() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Emp emp = session.getMapper(Chapter05EmpMapper.class).many2manyByAnn(4);
            assertNotNull(emp);
            assertEquals("JONES", emp.getEname());
            List<Skill> skills = emp.getSkills();
            assertEquals(3, skills.size());
        }
    }

    @Test
    public void testMany2manyReverseByXml() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Skill skill = session.getMapper(Chapter05SkillMapper.class).findByIdWithEmployeesByXml(3);
            assertNotNull(skill);
            assertEquals("MyBatis", skill.getName());
            assertEquals(3, skill.getEmps().size());
            assertEquals("JONES", skill.getEmps().get(0).getEname());
        }
    }

    @Test
    public void testMany2manyReverseByAnnotation() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Skill skill = session.getMapper(Chapter05SkillMapper.class).findByIdWithEmployeesByAnnotation(3);
            assertNotNull(skill);
            assertEquals("MyBatis", skill.getName());
            assertEquals(3, skill.getEmps().size());
            assertEquals("JONES", skill.getEmps().get(0).getEname());
        }
    }

    @Test
    public void testXmlAndAnnotationResultsAgree() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Chapter05EmpMapper empMapper = session.getMapper(Chapter05EmpMapper.class);
            Emp xml = empMapper.one2oneByXml(1);
            Emp annotation = empMapper.one2oneByAnn(1);
            assertEmployeeValuesEqual(xml, annotation);
            assertDeptValuesEqual(xml.getDept(), annotation.getDept());

            List<Emp> xmlEmployees = empMapper.many2oneByXml();
            List<Emp> annotationEmployees = empMapper.many2oneByAnn();
            assertEquals(xmlEmployees.size(), annotationEmployees.size());
            for (int i = 0; i < xmlEmployees.size(); i++) {
                assertEmployeeValuesEqual(xmlEmployees.get(i), annotationEmployees.get(i));
                assertDeptValuesEqual(xmlEmployees.get(i).getDept(), annotationEmployees.get(i).getDept());
            }

            Chapter05DeptMapper deptMapper = session.getMapper(Chapter05DeptMapper.class);
            Dept deptXml = deptMapper.one2manyByXml(2);
            Dept deptAnnotation = deptMapper.one2manyByAnn(2);
            assertDeptValuesEqual(deptXml, deptAnnotation);
            assertEquals(deptXml.getEmps().size(), deptAnnotation.getEmps().size());
            for (int i = 0; i < deptXml.getEmps().size(); i++) {
                assertEmployeeValuesEqual(deptXml.getEmps().get(i), deptAnnotation.getEmps().get(i));
            }

            Emp skillsXml = empMapper.many2manyByXml(8);
            Emp skillsAnnotation = empMapper.many2manyByAnn(8);
            assertEmployeeValuesEqual(skillsXml, skillsAnnotation);
            assertEquals(skillsXml.getSkills().size(), skillsAnnotation.getSkills().size());
            for (int i = 0; i < skillsXml.getSkills().size(); i++) {
                Skill xmlSkill = skillsXml.getSkills().get(i);
                Skill annotationSkill = skillsAnnotation.getSkills().get(i);
                assertEquals(xmlSkill.getId(), annotationSkill.getId());
                assertEquals(xmlSkill.getName(), annotationSkill.getName());
                assertEquals(xmlSkill.getDescription(), annotationSkill.getDescription());
            }

            Chapter05SkillMapper skillMapper = session.getMapper(Chapter05SkillMapper.class);
            Skill reverseXml = skillMapper.findByIdWithEmployeesByXml(3);
            Skill reverseAnnotation = skillMapper.findByIdWithEmployeesByAnnotation(3);
            assertEquals(reverseXml.getId(), reverseAnnotation.getId());
            assertEquals(reverseXml.getName(), reverseAnnotation.getName());
            assertEquals(reverseXml.getEmps().size(), reverseAnnotation.getEmps().size());
            for (int i = 0; i < reverseXml.getEmps().size(); i++) {
                assertEmployeeValuesEqual(reverseXml.getEmps().get(i), reverseAnnotation.getEmps().get(i));
            }
        }
    }

    @Test
    public void testSqlStatementCountsForJoinAndNestedSelects() {
        Chapter05EmpMapper empMapper;
        Chapter05DeptMapper deptMapper;

        try (SqlSession session = sqlSessionFactory.openSession()) {
            empMapper = session.getMapper(Chapter05EmpMapper.class);
            sqlStatementCounter.reset();
            Emp employee = empMapper.one2oneByXml(1);
            assertEquals("RESEARCH", employee.getDept().getDname());
            assertEquals(1, sqlStatementCounter.getCount());
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            empMapper = session.getMapper(Chapter05EmpMapper.class);
            sqlStatementCounter.reset();
            Emp employee = empMapper.one2oneByXmlSelect(1);
            assertEquals("RESEARCH", employee.getDept().getDname());
            assertEquals(2, sqlStatementCounter.getCount());
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            empMapper = session.getMapper(Chapter05EmpMapper.class);
            sqlStatementCounter.reset();
            List<Emp> employees = empMapper.many2oneByXml();
            for (Emp employee : employees) {
                employee.getDept().getDname();
            }
            assertEquals(1, sqlStatementCounter.getCount());
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            empMapper = session.getMapper(Chapter05EmpMapper.class);
            sqlStatementCounter.reset();
            List<Emp> employees = empMapper.many2oneByXmlSelect();
            Set<Integer> departmentIds = new HashSet<>();
            for (Emp employee : employees) {
                departmentIds.add(employee.getDeptno());
                employee.getDept().getDname();
            }
            assertEquals(1 + departmentIds.size(), sqlStatementCounter.getCount());
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            deptMapper = session.getMapper(Chapter05DeptMapper.class);
            sqlStatementCounter.reset();
            Dept dept = deptMapper.one2manyByXml(2);
            assertEquals(5, dept.getEmps().size());
            assertEquals(1, sqlStatementCounter.getCount());
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            deptMapper = session.getMapper(Chapter05DeptMapper.class);
            sqlStatementCounter.reset();
            Dept dept = deptMapper.one2manyByAnn(2);
            assertEquals(5, dept.getEmps().size());
            assertEquals(2, sqlStatementCounter.getCount());
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            empMapper = session.getMapper(Chapter05EmpMapper.class);
            sqlStatementCounter.reset();
            Emp employee = empMapper.many2manyByXml(8);
            assertEquals(3, employee.getSkills().size());
            assertEquals(1, sqlStatementCounter.getCount());
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            empMapper = session.getMapper(Chapter05EmpMapper.class);
            sqlStatementCounter.reset();
            Emp employee = empMapper.many2manyByAnn(8);
            assertEquals(3, employee.getSkills().size());
            assertEquals(2, sqlStatementCounter.getCount());
        }
    }

    private void assertEmployeeValuesEqual(Emp expected, Emp actual) {
        assertEquals(expected.getEmpno(), actual.getEmpno());
        assertEquals(expected.getEname(), actual.getEname());
        assertEquals(expected.getJob(), actual.getJob());
        assertEquals(expected.getDeptno(), actual.getDeptno());
        assertEquals(expected.getSal(), actual.getSal());
    }

    private void assertDeptValuesEqual(Dept expected, Dept actual) {
        assertEquals(expected.getDeptno(), actual.getDeptno());
        assertEquals(expected.getDname(), actual.getDname());
        assertEquals(expected.getLoc(), actual.getLoc());
    }
}
