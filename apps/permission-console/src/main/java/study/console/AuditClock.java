package study.console;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import java.time.LocalDateTime;
public final class AuditClock implements MetaObjectHandler {
    public void insertFill(MetaObject record) {
        LocalDateTime stamp=LocalDateTime.now();
        for(String key:new String[]{"createTime","updateTime"})
            if(getFieldValByName(key,record)==null) setFieldValByName(key,stamp,record);
    }
    public void updateFill(MetaObject record) { setFieldValByName("updateTime",LocalDateTime.now(),record); }
}
