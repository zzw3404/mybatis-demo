package com.example.chapter05.mapper;

import com.example.chapter05.entity.Dept;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface Chapter05DeptMapper {

    @Select("SELECT deptno, dname, loc FROM dept WHERE deptno = #{deptno}")
    Dept findById(Integer deptno);

    Dept one2manyByXml(Integer deptno);

    @Select("SELECT deptno, dname, loc FROM dept WHERE deptno = #{deptno}")
    @Results(id = "deptWithEmpsByAnnotation", value = {
            @Result(id = true, property = "deptno", column = "deptno"),
            @Result(property = "dname", column = "dname"),
            @Result(property = "loc", column = "loc"),
            @Result(property = "emps", column = "deptno",
                    many = @Many(select = "com.example.chapter05.mapper.Chapter05EmpMapper.selectListByDeptno"))
    })
    Dept one2manyByAnn(Integer deptno);
}
