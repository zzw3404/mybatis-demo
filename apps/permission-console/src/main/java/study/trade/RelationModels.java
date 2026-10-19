package study.trade;
import java.time.LocalDate;
import java.util.*;
public final class RelationModels {
 public static class Profile {public Long customerId;public String memberNo,memberLevel;public LocalDate registeredOn;
  public String toString(){return "会员档案{客户编号="+customerId+", 会员号="+memberNo+", 等级="+memberLevel+", 注册日="+registeredOn+"}";}}
 public static class Customer {public Long id;public String customerName;public Profile profile;public List<Sale> orders=new ArrayList<>();
  public String toString(){return "客户{编号="+id+", 姓名="+customerName+"}";}}
 public static class Sale {public Long id;public String orderNo;public List<Item> products=new ArrayList<>();
  public String toString(){return "订单{编号="+id+", 订单号="+orderNo+"}";}}
 public static class Item {public Long id;public String productName;public List<Sale> orders=new ArrayList<>();public List<Supplier> suppliers=new ArrayList<>();
  public String toString(){return "商品{编号="+id+", 名称="+productName+"}";}}
 public static class Supplier {public Long id;public String supplierName;public List<Item> products=new ArrayList<>();
  public String toString(){return "供应商{编号="+id+", 名称="+supplierName+"}";}}
}
