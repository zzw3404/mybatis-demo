package study.console;
import com.baomidou.mybatisplus.core.conditions.query.*;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.*;
import static study.console.Database.Intent.*;

public final class StudentLab {
    public enum Pattern { CONTAINS, STARTS_WITH, ENDS_WITH }
    public static final class Criteria {
        public String major,name,sort="id";
        public Double minimum,maximum;
        public Integer alternativeAge;
        public List<String> majors=List.of();
        public Pattern pattern=Pattern.CONTAINS;
    }
    public record Query<T>(String sql,T data) { }
    private final Database db;
    public StudentLab(Database db){this.db=db;}
    public Learner find(long id){return db.plusWork(READ,m->m.selectById(id));}
    public long count(){return db.plusWork(READ,m->m.selectCount(null));}
    public long count(String major){return db.plusWork(READ,m->m.selectCount(new LambdaQueryWrapper<Learner>().eq(Learner::getMajor,major)));}
    public long occurrences(String name){return db.plusWork(READ,m->m.selectCount(new LambdaQueryWrapper<Learner>().eq(Learner::getName,name)));}
    public Learner unique(String name){return db.plusWork(READ,m->m.selectOne(new LambdaQueryWrapper<Learner>().eq(Learner::getName,name)));}
    public Page<Learner> page(int number,int size){
        return page(number,size,new Criteria(),true);
    }
    public Page<Learner> page(int number,int size,Criteria criteria,boolean lambda){
        if(number<1 || size<1 || size>500)throw new IllegalArgumentException("页码需为正数，每页1至500条");
        return db.plusWork(READ,m->m.selectPage(new Page<>(number,size),lambda?references(criteria):columns(criteria)));
    }
    public Learner enroll(Learner row){if(row.getName()==null || row.getName().isBlank())throw new IllegalArgumentException("学生姓名必填");db.plusWork(COMMIT,m->m.insert(row));return find(row.getId());}
    public boolean changeScore(long id,double score,Integer version){
        return db.plusWork(COMMIT,m->{Learner row=m.selectById(id);if(row==null)return false;
            if(version!=null)row.setVersion(version);row.setScore(score);return m.updateById(row)==1;});
    }
    public int expel(Collection<Long> ids){return ids.isEmpty() ? 0 : db.plusWork(COMMIT,m->m.deleteByIds(ids));}
    public List<Map<String,Object>> stats(){return db.plusWork(READ,LearnerMapper::statistics);}
    public List<Learner> top(double floor,int size){if(size<1)throw new IllegalArgumentException("数量必须大于0");return db.plusWork(READ,m->m.top(floor,size));}
    private void check(Criteria f){
        if(f.minimum!=null && f.maximum!=null && f.minimum>f.maximum)throw new IllegalArgumentException("最低分不能大于最高分");
        if(!Set.of("id","score","age","major").contains(f.sort))throw new IllegalArgumentException("排序字段无效");
    }
    private QueryWrapper<Learner> columns(Criteria f){
        check(f);QueryWrapper<Learner> q=new QueryWrapper<>();
        if(f.major!=null)q.eq("major",f.major);
        if(!f.majors.isEmpty())q.in("major",f.majors);
        if(f.minimum!=null)q.ge("score",f.minimum);if(f.maximum!=null)q.le("score",f.maximum);
        if(f.name!=null || f.alternativeAge!=null)q.and(group->{
            if(f.name!=null){switch(f.pattern){case CONTAINS->group.like("name",f.name);case STARTS_WITH->group.likeRight("name",f.name);case ENDS_WITH->group.likeLeft("name",f.name);}}
            if(f.alternativeAge!=null){if(f.name!=null)group.or();group.gt("age",f.alternativeAge);}
        });
        return "score".equals(f.sort) ? q.orderByDesc(f.sort).orderByAsc("id") : q.orderByAsc(f.sort).orderByAsc("id");
    }
    private LambdaQueryWrapper<Learner> references(Criteria f){
        check(f);LambdaQueryWrapper<Learner> q=new LambdaQueryWrapper<>();
        if(f.major!=null)q.eq(Learner::getMajor,f.major);if(!f.majors.isEmpty())q.in(Learner::getMajor,f.majors);
        if(f.minimum!=null)q.ge(Learner::getScore,f.minimum);if(f.maximum!=null)q.le(Learner::getScore,f.maximum);
        if(f.name!=null || f.alternativeAge!=null)q.and(group->{
            if(f.name!=null){switch(f.pattern){case CONTAINS->group.like(Learner::getName,f.name);case STARTS_WITH->group.likeRight(Learner::getName,f.name);case ENDS_WITH->group.likeLeft(Learner::getName,f.name);}}
            if(f.alternativeAge!=null){if(f.name!=null)group.or();group.gt(Learner::getAge,f.alternativeAge);}
        });
        switch(f.sort){case "score"->q.orderByDesc(Learner::getScore);case "age"->q.orderByAsc(Learner::getAge);case "major"->q.orderByAsc(Learner::getMajor);default->{}}
        return q.orderByAsc(Learner::getId);
    }
    public Query<List<Learner>> query(Criteria criteria,boolean lambda){
        if(lambda){var q=references(criteria);return db.plusWork(READ,m->new Query<>(q.getTargetSql(),m.selectList(q)));}
        var q=columns(criteria);return db.plusWork(READ,m->new Query<>(q.getTargetSql(),m.selectList(q)));
    }
    public Query<List<Learner>> slim(Criteria criteria){var q=columns(criteria).select("id","name","major","score");return db.plusWork(READ,m->new Query<>(q.getTargetSql(),m.selectList(q)));}
    public Query<List<Map<String,Object>>> maps(Criteria criteria){var q=columns(criteria).select("id","name","major","score");return db.plusWork(READ,m->new Query<>(q.getTargetSql(),m.selectMaps(q)));}
    public List<Map<String,Object>> grouped(){return db.plusWork(READ,m->m.selectMaps(new QueryWrapper<Learner>().select("major","COUNT(*) AS total","AVG(score) AS average","MIN(score) AS minimum","MAX(score) AS maximum").groupBy("major").orderByAsc("major")));}
    public List<Learner> exact(Map<String,Object> fields){
        if(fields.isEmpty() || !Set.of("id","name","age","major","score","email").containsAll(fields.keySet()))throw new IllegalArgumentException("等值查询条件为空或字段无效");
        return db.plusWork(READ,m->m.selectByMap(fields));
    }
    public int raise(String major,double threshold,double goal){
        if(major==null || major.isBlank())throw new IllegalArgumentException("批量更新必须指定专业");
        Learner change=new Learner();change.setScore(goal);
        return db.plusWork(COMMIT,m->m.update(change,new LambdaUpdateWrapper<Learner>().eq(Learner::getMajor,major).lt(Learner::getScore,threshold)));
    }
    public Map<String,Object> rehearse(){
        return db.plusWork(REHEARSE,m->{Learner draft=new Learner();draft.setName("实验学生");draft.setAge(22);draft.setMajor("事务实验");draft.setScore(70.0);
            m.insert(draft);long id=draft.getId();Learner first=m.selectById(id);Integer old=first.getVersion();
            first.setScore(90.0);int updated=m.updateById(first);
            Learner stale=new Learner();stale.setId(id);stale.setVersion(old);stale.setScore(10.0);int rejected=m.updateById(stale);
            int removed=m.deleteById(id);return Map.of("id",id,"generatedTime",draft.getCreateTime().toString(),"updated",updated,"staleUpdated",rejected,"removed",removed,"deletedFlag",m.deletionFlag(id),"rolledBack",true);});
    }
    public Map<String,Object> rehearseWrappers(){
        return db.plusWork(REHEARSE,m->{
            Learner x=new Learner();x.setName("实验同名");x.setMajor("实验专业");x.setScore(65.0);m.insert(x);
            Learner y=new Learner();y.setName("实验同名");y.setMajor("实验专业");y.setScore(75.0);m.insert(y);
            Learner set=new Learner();set.setScore(96.0);
            int affected=m.update(set,new LambdaUpdateWrapper<Learner>().eq(Learner::getMajor,"实验专业").lt(Learner::getScore,90));
            Learner partial=new Learner();partial.setId(x.getId());partial.setEmail("trial@example.com");partial.setScore(null);m.updateById(partial);
            Double kept=m.selectById(x.getId()).getScore();
            m.update(null,new LambdaUpdateWrapper<Learner>().eq(Learner::getId,x.getId()).set(Learner::getScore,null));
            boolean explicitNull=m.selectById(x.getId()).getScore()==null;
            boolean duplicate=false;try{m.selectOne(new LambdaQueryWrapper<Learner>().eq(Learner::getName,"实验同名"));}catch(org.apache.ibatis.exceptions.TooManyResultsException e){duplicate=true;}
            return Map.of("matched",affected,"preservedScore",kept,"explicitNull",explicitNull,"duplicateRejected",duplicate,"rolledBack",true);
        });
    }
    public void reset(){if(db.usesProjectTables())throw new IllegalArgumentException("当前使用真实 mp_student，不能清空已有学生数据");db.runScript("database/reset-students.sql");}
}
