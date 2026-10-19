package study.console;
import org.junit.Test;
import static org.junit.Assert.*;
public class LocalCacheTest {
    @Test public void jdbcCountsProveCacheHitsAndInvalidation(){
        try(Database db=Database.demo()){
            var lab=new LocalCacheLab(db);var outcomes=lab.run();assertEquals(8,outcomes.size());
            for(var outcome:outcomes)assertTrue(outcome.scenario()+"\n"+outcome.log(),outcome.expected());
            assertEquals(1,outcomes.get(0).actualSql());assertEquals(3,outcomes.get(4).actualSql());
            assertEquals(8,new StudentLab(db).count());
        }
    }
}
