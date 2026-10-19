package study.trade;
import java.util.*;
import java.math.BigDecimal;
import org.apache.ibatis.annotations.Param;
import study.trade.TradeModels.*;
public interface TradeMapper {
 List<Product> findAdvanced(ProductQuery query);
 int insertBatch(@Param("products")List<Product> products);
 int updateSelective(Product row);
 List<Product> productsWithBrands();
 List<Map<String,Object>> brandSummary();
 List<Product> selectAll();Product findById(long id);
 List<Product> selectByCondition(@Param("category")String category,@Param("minimum")BigDecimal minimum,@Param("keyword")String keyword);
 long countProducts();List<Product> page(@Param("offset")int offset,@Param("size")int size);
 int insert(Product row);int updateStock(@Param("id")long id,@Param("stock")int stock);int delete(long id);
 List<Detail> selectTradeDetails(@Param("status")String status,@Param("customer")String customer);
 Order findOrderWithLines(long id);List<Detail> linesForOrder(long id);
 List<Order> ordersForCustomer(long id);
 List<Map<String,Object>> categorySummary();
}
