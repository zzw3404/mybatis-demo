package study.console;
import org.apache.ibatis.annotations.Param;
import java.util.*;
public interface TradeMapper {
    List<Map<String,Object>> vendors();
    List<Map<String,Object>> goods();
    List<Map<String,Object>> buyers();
    List<Map<String,Object>> inventory();
    List<Map<String,Object>> catalog();
    List<Map<String,Object>> sold(Map<String,Object> filters);
    List<Map<String,Object>> soldKinds(@Param("id") long id);
    List<Map<String,Object>> purchasingClients(@Param("id") long id);
    List<Map<String,Object>> serviceClients(@Param("id") long id);
    List<Map<String,Object>> serviceRows(@Param("id") long id);
    Models.Vendor profile(@Param("id") long id);
}
