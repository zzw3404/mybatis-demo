package study.trade;
import java.math.BigDecimal;
import java.util.*;
public final class ProductQuery {
 public String keyword,category;public List<Long> brandIds=List.of();public BigDecimal minimum,maximum;public Boolean priceDesc;
 public void validate(){if(minimum!=null&&maximum!=null&&minimum.compareTo(maximum)>0)throw new IllegalArgumentException("最低价不能高于最高价");}
}
