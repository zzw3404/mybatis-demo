package study.console;
import com.baomidou.mybatisplus.annotation.*;
@TableName("learner")
public class Learner {
    @TableId(type=IdType.AUTO)
    private Long id;

    private String name;

    private Integer age;

    private String email;

    private String major;
    @TableField(updateStrategy=FieldStrategy.NOT_NULL)
    private Double score;
    @Version
    private Integer version;
    @TableLogic
    private Integer deleted;
    @TableField(fill=FieldFill.INSERT)
    private java.time.LocalDateTime createTime;
    @TableField(fill=FieldFill.INSERT_UPDATE)
    private java.time.LocalDateTime updateTime;
    public Long getId() { return id; }
    public void setId(Long value) { id=value; }
    public String getName() { return name; }
    public void setName(String value) { name=value; }
    public Integer getAge() { return age; }
    public void setAge(Integer value) { age=value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email=value; }
    public String getMajor() { return major; }
    public void setMajor(String value) { major=value; }
    public Double getScore() { return score; }
    public void setScore(Double value) { score=value; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer value) { version=value; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer value) { deleted=value; }
    public java.time.LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(java.time.LocalDateTime value) { createTime=value; }
    public java.time.LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(java.time.LocalDateTime value) { updateTime=value; }
}
