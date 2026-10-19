package study.console;
import java.nio.file.*;
import java.io.*;
import java.util.Properties;
public final class ProjectConnection {
    private ProjectConnection() { }
    public static Path configFile(){
        String explicit=System.getProperty("app.project.db",System.getenv("PROJECT_DB_CONFIG"));
        if(explicit!=null)return Path.of(explicit).toAbsolutePath();
        try{
            Path location=Path.of(ProjectConnection.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            for(Path parent=location;parent!=null;parent=parent.getParent()){
                Path candidate=parent.resolve("src/main/resources/common/db.properties");
                if(Files.isRegularFile(candidate))return candidate;
            }
        }catch(Exception e){throw new IllegalStateException("无法定位原项目连接配置",e);}
        throw new IllegalStateException("找不到原项目 common/db.properties，请设置 PROJECT_DB_CONFIG");
    }
    public static Properties settings(){
        Properties settings=new Properties();
        try(Reader reader=Files.newBufferedReader(configFile())){settings.load(reader);}
        catch(IOException e){throw new IllegalStateException("无法读取原项目数据库配置",e);}
        String[][] names={{"jdbc.url","PERMISSION_DB_URL"},{"jdbc.driver","PERMISSION_DB_DRIVER"},{"jdbc.username","PERMISSION_DB_USERNAME"},{"jdbc.password","PERMISSION_DB_PASSWORD"}};
        for(String[] pair:names){String value=System.getenv(pair[1]);if(value!=null)settings.setProperty(pair[0],value);}
        String url=settings.getProperty("jdbc.url","");
        if(!url.startsWith("jdbc:mysql:"))throw new IllegalStateException("运行控制台需要MySQL连接，不再默认使用内存演示库");
        if(!url.contains("createDatabaseIfNotExist="))url+=(url.contains("?")?"&":"?")+"createDatabaseIfNotExist=true";
        else url=url.replace("createDatabaseIfNotExist=false","createDatabaseIfNotExist=true");
        settings.setProperty("jdbc.url",url);return settings;
    }
}