package study.trade;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
public final class TradeModels {
 private TradeModels(){}
 public static class Brand implements Serializable {
  private static final long serialVersionUID=1L;
  public Long id;public String brandName,website;
  public String toString(){return "品牌{编号="+id+", 名称="+brandName+", 网站="+website+"}";}
 }
 @TableName("lab_trade_product")
 public static class Product implements Serializable {
  private static final long serialVersionUID=1L;
  @TableId(type=IdType.INPUT)
  public Long id;public String productName,category,brand,saleStatus;public BigDecimal price;public Integer stock;
  public Long brandId;
  @Version public Integer version;
  @TableField(fill=FieldFill.INSERT) public LocalDateTime createTime;
  @TableField(fill=FieldFill.INSERT_UPDATE) public LocalDateTime updateTime;
  @TableField(exist=false) public Brand brandInfo;
  public Long getId(){return id;} public void setId(Long value){id=value;}
  public String getProductName(){return productName;} public void setProductName(String value){productName=value;}
  public String getCategory(){return category;} public void setCategory(String value){category=value;}
  public String getBrand(){return brand;} public void setBrand(String value){brand=value;}
  public String getSaleStatus(){return saleStatus;} public void setSaleStatus(String value){saleStatus=value;}
  public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal value){price=value;}
  public Integer getStock(){return stock;} public void setStock(Integer value){stock=value;}
  public Long getBrandId(){return brandId;} public void setBrandId(Long value){brandId=value;}
  public Integer getVersion(){return version;} public void setVersion(Integer value){version=value;}
  public LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(LocalDateTime value){createTime=value;}
  public LocalDateTime getUpdateTime(){return updateTime;} public void setUpdateTime(LocalDateTime value){updateTime=value;}
  public String toString(){return "商品{编号="+id+", 名称="+productName+", 分类="+category+", 品牌="+brand+", 单价="+price+", 库存="+stock+", 状态="+saleStatus+"}";}
 }
 public static class Detail implements Serializable {
  private static final long serialVersionUID=1L;
  public Long lineId;public String orderNo,customerName,productName,category,orderStatus;public Integer quantity;public BigDecimal dealPrice,subtotal;public LocalDate orderedOn;
  public String toString(){return "订单="+orderNo+"，客户="+customerName+"，商品="+productName+"，分类="+category+"，数量="+quantity+"，成交单价="+dealPrice+"，小计="+subtotal+"，日期="+orderedOn+"，状态="+orderStatus;}
 }
 public static class Order implements Serializable {
  private static final long serialVersionUID=1L;
  public Long id;public String orderNo,customerName,orderStatus;public LocalDate orderedOn;public List<Detail> lines=new ArrayList<>();
  public String toString(){return "订单{编号="+id+", 订单号="+orderNo+", 客户="+customerName+", 日期="+orderedOn+", 状态="+orderStatus+", 明细数="+lines.size()+"}";}
 }
}
