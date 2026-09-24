package com.example.chapter05.mapper;

import com.example.chapter05.entity.Emp;
import com.example.chapter05.entity.Skill;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface Chapter05EmpMapper {

    Emp one2oneByXml(Integer empno);

    @Select("SELECT empno, ename, job, sal, deptno FROM emp WHERE empno = #{empno}")
    @Results(id = "empWithDeptByAnnotation", value = {
            @Result(id = true, property = "empno", column = "empno"),
            @Result(property = "ename", column = "ename"),
            @Result(property = "job", column = "job"),
            @Result(property = "sal", column = "sal"),
            @Result(property = "deptno", column = "deptno"),
            @Result(property = "dept", column = "deptno",
                    one = @One(select = "com.example.chapter05.mapper.Chapter05DeptMapper.findById"))
    })
    Emp one2oneByAnn(Integer empno);

    List<Emp> many2oneByXml();

    @Select("SELECT empno, ename, job, sal, deptno FROM emp ORDER BY empno")
    @Results(id = "empListWithDeptByAnnotation", value = {
            @Result(id = true, property = "empno", column = "empno"),
            @Result(property = "ename", column = "ename"),
            @Result(property = "job", column = "job"),
            @Result(property = "sal", column = "sal"),
            @Result(property = "deptno", column = "deptno"),
            @Result(property = "dept", column = "deptno",
                    one = @One(select = "com.example.chapter05.mapper.Chapter05DeptMapper.findById"))
    })
    List<Emp> many2oneByAnn();

    Emp many2manyByXml(Integer empno);

    @Select("SELECT empno, ename, job, sal, deptno FROM emp WHERE empno = #{empno}")
    @Results(id = "empWithSkillsByAnnotation", value = {
            @Result(id = true, property = "empno", column = "empno"),
            @Result(property = "ename", column = "ename"),
            @Result(property = "job", column = "job"),
            @Result(property = "sal", column = "sal"),
            @Result(property = "deptno", column = "deptno"),
            @Result(property = "skills", column = "empno",
                    many = @Many(select = "com.example.chapter05.mapper.Chapter05EmpMapper.selectSkillsByEmpno"))
    })
    Emp many2manyByAnn(Integer empno);

    @Select("SELECT s.id, s.name, s.description FROM skill s "
            + "INNER JOIN emp_skill es ON s.id = es.skill_id WHERE es.empno = #{empno} ORDER BY s.id")
    List<Skill> selectSkillsByEmpno(@Param("empno") Integer empno);

    @Select("SELECT empno, ename, job, mgr, hiredate, sal, comm, deptno "
            + "FROM emp WHERE deptno = #{deptno} ORDER BY empno")
    List<Emp> selectListByDeptno(@Param("deptno") Integer deptno);
}
