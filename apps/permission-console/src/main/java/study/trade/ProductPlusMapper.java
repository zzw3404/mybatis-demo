package study.trade;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
@org.apache.ibatis.annotations.CacheNamespaceRef(TradeMapper.class)
public interface ProductPlusMapper extends BaseMapper<TradeModels.Product> {}
