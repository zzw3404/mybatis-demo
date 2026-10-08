package com.example.chapter08.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Chapter 08 entity for learning MyBatis-Plus CRUD and field mapping. */
@TableName("mp_student")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer age;
    private String email;
    private String major;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Double score;

    @Version
    private Integer version;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Student() {
    }

    public Student(String name, Integer age, String email, String major, Double score) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.major = major;
        this.score = score;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    @Override
    public String toString() {
        return "Student{" + "id=" + id + ", name='" + name + '\''
                + ", age=" + age + ", email='" + email + '\''
                + ", major='" + major + '\'' + ", score=" + score
                + ", version=" + version + ", deleted=" + deleted
                + ", createTime=" + createTime + ", updateTime=" + updateTime + '}';
    }
}
