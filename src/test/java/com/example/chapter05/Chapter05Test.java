package com.example.chapter05;

import com.example.chapter05.entity.Dept;
import com.example.chapter05.entity.Emp;
import com.example.chapter05.entity.Skill;
import com.example.chapter05.mapper.Chapter05DeptMapper;
import com.example.chapter05.mapper.Chapter05EmpMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/** Adapted from Documents/chapter05/chapter05test/Chapter05Test.java. */
public class Chapter05Test {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws Exception {
        try (InputStream inputStream = Resources.getResourceAsStream("chapter05/mybatis-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        }
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
            assertEquals("ACCOUNTING", emps.get(0).getDept().getDname());
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
    public void testXmlAndAnnotationResultsAgree() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Chapter05EmpMapper empMapper = session.getMapper(Chapter05EmpMapper.class);
            Emp xml = empMapper.one2oneByXml(1);
            Emp annotation = empMapper.one2oneByAnn(1);
            assertEquals(xml.getEname(), annotation.getEname());
            assertEquals(xml.getDept().getDname(), annotation.getDept().getDname());

            Chapter05DeptMapper deptMapper = session.getMapper(Chapter05DeptMapper.class);
            Dept deptXml = deptMapper.one2manyByXml(2);
            Dept deptAnnotation = deptMapper.one2manyByAnn(2);
            assertEquals(deptXml.getDname(), deptAnnotation.getDname());
            assertEquals(deptXml.getEmps().size(), deptAnnotation.getEmps().size());
        }
    }
}
