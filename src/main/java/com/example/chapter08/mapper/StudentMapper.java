package com.example.chapter08.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chapter08.entity.Student;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** MyBatis-Plus generates the standard CRUD statements for this mapper. */
public interface StudentMapper extends BaseMapper<Student> {

    /**
     * MyBatis-Plus has no built-in pessimistic-lock annotation; use database row locking
     * explicitly and call this method inside an open, non-autocommit transaction.
     */
    @Select("SELECT * FROM mp_student WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    Student selectByIdForUpdate(@Param("id") Long id);
}
