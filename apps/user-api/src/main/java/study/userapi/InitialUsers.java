package study.userapi;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;
@Component
public final class InitialUsers implements ApplicationRunner {
    private final JdbcTemplate sql;
    public InitialUsers(JdbcTemplate sql){this.sql=sql;}
    public void run(ApplicationArguments args){
        Long count=sql.queryForObject("SELECT COUNT(*) FROM `user`",Long.class);
        if(count!=null&&count==0)new ResourceDatabasePopulator(new ClassPathResource("database/users-seed.sql")).execute(sql.getDataSource());
    }
}