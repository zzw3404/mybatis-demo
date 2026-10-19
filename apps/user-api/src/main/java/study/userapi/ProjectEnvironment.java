package study.userapi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
/** Both application entry points resolve the same existing project connection. */
public final class ProjectEnvironment implements EnvironmentPostProcessor,Ordered {
    public int getOrder(){return Ordered.LOWEST_PRECEDENCE;}
    public void postProcessEnvironment(ConfigurableEnvironment environment,SpringApplication application){
        // Explicit datasource properties, including each isolated test database, retain precedence.
        if(environment.containsProperty("spring.datasource.url"))return;
        Properties p=new Properties();
        try(Reader reader=Files.newBufferedReader(findConfig(environment))){p.load(reader);}
        catch(IOException e){throw new IllegalStateException("无法读取原项目common/db.properties",e);}
        String url=value(environment,"USER_DB_URL","PERMISSION_DB_URL",p.getProperty("jdbc.url"));
        if(url==null||!url.startsWith("jdbc:mysql:"))throw new IllegalStateException("用户接口需要配置MySQL，不再默认使用内存数据库");
        if(!url.contains("createDatabaseIfNotExist="))url+=(url.contains("?")?"&":"?")+"createDatabaseIfNotExist=true";
        else url=url.replace("createDatabaseIfNotExist=false","createDatabaseIfNotExist=true");
        Map<String,Object> data=new LinkedHashMap<>();data.put("spring.datasource.url",url);
        data.put("spring.datasource.driver-class-name",value(environment,"USER_DB_DRIVER","PERMISSION_DB_DRIVER",p.getProperty("jdbc.driver","com.mysql.cj.jdbc.Driver")));
        data.put("spring.datasource.username",value(environment,"USER_DB_USERNAME","PERMISSION_DB_USERNAME",p.getProperty("jdbc.username")));
        data.put("spring.datasource.password",value(environment,"USER_DB_PASSWORD","PERMISSION_DB_PASSWORD",p.getProperty("jdbc.password","")));
        environment.getPropertySources().addLast(new MapPropertySource("original-project-database",data));
    }
    private static String value(Environment e,String first,String second,String fallback){String x=e.getProperty(first);return x!=null?x:e.getProperty(second,fallback);}
    private static Path findConfig(Environment environment){
        String specified=environment.getProperty("app.project.db",environment.getProperty("PROJECT_DB_CONFIG"));if(specified!=null)return Path.of(specified);
        try{Path path=Path.of(ProjectEnvironment.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            for(Path next=path;next!=null;next=next.getParent()){Path candidate=next.resolve("src/main/resources/common/db.properties");if(Files.isRegularFile(candidate))return candidate;}
        }catch(Exception e){throw new IllegalStateException("无法定位原项目数据库配置",e);}
        throw new IllegalStateException("请设置PROJECT_DB_CONFIG指向原项目common/db.properties");
    }
}