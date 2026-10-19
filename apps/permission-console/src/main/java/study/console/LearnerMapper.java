package study.console;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.*;
public interface LearnerMapper extends BaseMapper<Learner> {
    // MP 3.5.17's cursor-based default opens a second session. Keep single-row
    // selection on this mapper's session so rehearsals see their own uncommitted rows.
    @Override default Learner selectOne(com.baomidou.mybatisplus.core.conditions.Wrapper<Learner> condition, boolean strict) {
        List<Learner> records=selectList(condition);
        if(strict && records.size()>1) throw new org.apache.ibatis.exceptions.TooManyResultsException("单条查询返回了"+records.size()+"条记录");
        return records.isEmpty()?null:records.get(0);
    }
    @Select("SELECT major,COUNT(*) AS total,AVG(score) AS average,MIN(score) AS minimum,MAX(score) AS maximum FROM learner WHERE deleted=0 GROUP BY major ORDER BY major")
    List<Map<String,Object>> statistics();
    @Select("SELECT * FROM learner WHERE deleted=0 AND score >= #{floor} ORDER BY score DESC,id LIMIT #{size}")
    List<Learner> top(@Param("floor") double floor,@Param("size") int size);
    @Select("SELECT deleted FROM learner WHERE id=#{id}") Integer deletionFlag(long id);
    @Select("SELECT * FROM learner WHERE id=#{id} FOR UPDATE") Learner locked(long id);
}
