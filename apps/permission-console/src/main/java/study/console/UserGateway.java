package study.console;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.Map;
/** Uses the six real Spring Boot endpoints; starts the bundled application on first use. */
public class UserGateway implements AutoCloseable {
    private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final String base;
    private Process owned;
    public UserGateway(){base=System.getenv().getOrDefault("USER_API_BASE","http://127.0.0.1:"+System.getenv().getOrDefault("USER_API_PORT","8080"));}
    public UserGateway(String base){this.base=base;}
    public Object call(String method,String path,Map<String,Object> payload){
        ensureServer();
        try{
            var request=HttpRequest.newBuilder(URI.create(base+path)).timeout(Duration.ofSeconds(10)).header("Content-Type","application/json");
            request.method(method,payload==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(JsonCodec.encode(payload)));
            var response=client.send(request.build(),HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
            if(response.statusCode()>=400)throw new IllegalArgumentException("用户接口返回"+response.statusCode()+"："+response.body());
            String text=response.body();if(text.isBlank())return null;
            try{return JsonCodec.MAPPER.readTree(text);}catch(Exception plainText){return text;}
        }catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("用户接口请求被中断",e);}
        catch(java.io.IOException e){throw new IllegalStateException("无法连接用户接口",e);}
    }
    private boolean reachable(){
        try{
            var response=client.send(HttpRequest.newBuilder(URI.create(base+"/user/database")).timeout(Duration.ofSeconds(2)).GET().build(),HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()!=200)throw new IllegalStateException("用户接口端口上仍是旧版服务，请关闭旧用户服务后重启控制台，或更换USER_API_PORT");
            var identity=JsonCodec.MAPPER.readTree(response.body());String expected=ProjectConnection.settings().getProperty("jdbc.url");
            var uri=URI.create(expected.substring(5));String catalog=uri.getPath().replace("/","");
            if(!"MySQL".equalsIgnoreCase(identity.path("engine").asText())||!catalog.equals(identity.path("database").asText()))throw new IllegalStateException("用户接口连接的数据库与控制台不同，请关闭旧服务后重启");
            return true;
        }catch(InterruptedException e){Thread.currentThread().interrupt();return false;}catch(java.io.IOException e){return false;}
    }
    private synchronized void ensureServer(){
        if(reachable())return;
        if(!base.startsWith("http://127.0.0.1:")&&!base.startsWith("http://localhost:"))throw new IllegalStateException("请先启动指定的用户接口服务");
        try{
            Path jar=Path.of(UserGateway.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path apps=Files.isRegularFile(jar)?jar.getParent().getParent().getParent():jar.getParent().getParent().getParent();
            Path api=apps.resolve("user-api/target/user-api-1.0-SNAPSHOT.jar");
            if(!Files.isRegularFile(api))throw new IllegalStateException("用户接口尚未打包，请运行 build-apps.cmd");
            int port=URI.create(base).getPort();Path java=Path.of(System.getProperty("java.home"),"bin","java.exe");
            var builder=new ProcessBuilder(java.toString(),"-Dfile.encoding=UTF-8","-Dapp.project.db="+ProjectConnection.configFile(),"-jar",api.toString(),"--server.port="+port);
            builder.redirectErrorStream(true).redirectOutput(api.getParent().resolve("console-server.log").toFile());
            owned=builder.start();
            long deadline=System.nanoTime()+Duration.ofSeconds(30).toNanos();
            while(System.nanoTime()<deadline&&owned.isAlive()){
                if(reachable())return;
                Thread.sleep(150);
            }
            close();throw new IllegalStateException("用户接口启动失败，请查看 apps/user-api/target/console-server.log");
        }catch(InterruptedException e){Thread.currentThread().interrupt();close();throw new IllegalStateException("启动被中断",e);}
        catch(Exception e){close();throw new IllegalStateException(e.getMessage(),e);}
    }
    public void close(){if(owned!=null){owned.destroy();try{if(!owned.waitFor(3,java.util.concurrent.TimeUnit.SECONDS))owned.destroyForcibly();}catch(InterruptedException e){owned.destroyForcibly();Thread.currentThread().interrupt();}owned=null;}}
    public static String escaped(String text){return URLEncoder.encode(text,java.nio.charset.StandardCharsets.UTF_8);}
}
