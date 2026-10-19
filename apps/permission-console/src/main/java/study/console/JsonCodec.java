package study.console;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.*;
public final class JsonCodec {
    public static final ObjectMapper MAPPER=new ObjectMapper().registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private JsonCodec() { }
    public static String encode(Object value) {
        try{return MAPPER.writeValueAsString(value);}catch(Exception e){throw new IllegalArgumentException("无法编码结果",e);}
    }
    public static Map<String,Object> fields(byte[] bytes) {
        if(bytes.length==0)return new LinkedHashMap<>();
        try{return MAPPER.readValue(bytes,new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>(){});}
        catch(Exception e){throw new IllegalArgumentException("请求体必须为JSON对象",e);}
    }
}
