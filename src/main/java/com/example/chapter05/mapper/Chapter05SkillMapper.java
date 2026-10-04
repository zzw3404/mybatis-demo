package com.example.chapter05.mapper;

import com.example.chapter05.entity.Emp;
import com.example.chapter05.entity.Skill;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** Queries a skill together with the employees who have that skill. */
public interface Chapter05SkillMapper {

    Skill findByIdWithEmployeesByXml(Integer skillId);

    @Select("SELECT id, name, description FROM skill WHERE id = #{skillId}")
    @Results(id = "skillWithEmployeesByAnnotation", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "emps", column = "id",
                    many = @Many(select = "com.example.chapter05.mapper.Chapter05SkillMapper.findEmployeesBySkillId"))
    })
    Skill findByIdWithEmployeesByAnnotation(@Param("skillId") Integer skillId);

    @Select("SELECT e.empno, e.ename, e.job, e.mgr, e.hiredate, e.sal, e.comm, e.deptno "
            + "FROM emp e INNER JOIN emp_skill es ON e.empno = es.empno "
            + "WHERE es.skill_id = #{skillId} ORDER BY e.empno")
    List<Emp> findEmployeesBySkillId(@Param("skillId") Integer skillId);
}
