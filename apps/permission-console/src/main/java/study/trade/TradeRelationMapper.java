package study.trade;
import java.util.*;
import study.trade.RelationModels.*;
public interface TradeRelationMapper {
 Customer findCustomerWithProfile(long id);List<Customer> findAllCustomersWithProfiles();
 Customer findCustomerWithOrders(long id);List<Customer> findAllCustomersWithOrders();
 Sale findOrderWithProducts(long id);Item findProductWithOrders(long id);
 Supplier findSupplierWithProducts(long id);Item findProductWithSuppliers(long id);
}
