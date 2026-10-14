package com.example.chapter05;

import com.example.chapter05.entity.Customer;
import com.example.chapter05.entity.Product;
import com.example.chapter05.entity.Supplier;
import com.example.chapter05.mapper.Chapter05CustomerMapper;
import com.example.chapter05.mapper.Chapter05ProductMapper;
import com.example.chapter05.mapper.Chapter05SupplierMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** Verifies nested XML collection mappings for the supplier/product/customer domain. */
public class Chapter05BusinessAssociationMapperTest {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws Exception {
        try (InputStream inputStream = Resources.getResourceAsStream("chapter05/mybatis-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        }
    }

    @Test
    public void testSupplierWithProducts() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Chapter05SupplierMapper mapper = session.getMapper(Chapter05SupplierMapper.class);
            Supplier supplier = mapper.findWithProductsById(1);
            assertNotNull(supplier);
            assertEquals("华东电子供应商", supplier.getSupplierName());
            assertEquals(3, supplier.getProducts().size());
            assertEquals("商务笔记本电脑", supplier.getProducts().get(0).getProductName());
            assertFalse(supplier.getProducts().get(2).getSold());
            assertTrue(supplier.getProducts().get(0).getServiceAccepted());

            List<Supplier> allSuppliers = mapper.findAllWithProducts();
            assertEquals(3, allSuppliers.size());

            List<Supplier> suppliersWithSales = mapper.findAllWithSoldProducts();
            assertEquals(3, suppliersWithSales.size());
            Supplier supplierOneSales = suppliersWithSales.get(0);
            assertEquals(1, supplierOneSales.getSupplierId().intValue());
            assertEquals(2, supplierOneSales.getProducts().size());
            assertTrue(supplierOneSales.getProducts().stream().allMatch(Product::getSold));
        }
    }

    @Test
    public void testSoldProductWithSuppliersAndCustomers() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Chapter05ProductMapper mapper = session.getMapper(Chapter05ProductMapper.class);
            List<Product> products = mapper.findSoldProductsWithSuppliersAndCustomers();
            assertEquals(4, products.size());

            Product laptop = mapper.findWithSuppliersAndCustomersById(1);
            assertNotNull(laptop);
            assertEquals("商务笔记本电脑", laptop.getProductName());
            assertTrue(laptop.getSold());
            assertTrue(laptop.getServiceAccepted());
            assertEquals(2, laptop.getSuppliers().size());
            assertEquals(1, laptop.getCustomers().size());
            assertEquals("星河科技有限公司", laptop.getCustomers().get(0).getCustomerName());

            Product mouse = mapper.findWithSuppliersAndCustomersById(2);
            assertTrue(mouse.getSold());
            assertFalse(mouse.getServiceAccepted());
            assertEquals(3, mouse.getSuppliers().size());
            assertEquals(2, mouse.getCustomers().size());
        }
    }

    @Test
    public void testCustomerWithPurchasedProductsAndSuppliers() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Chapter05CustomerMapper mapper = session.getMapper(Chapter05CustomerMapper.class);
            Customer customer = mapper.findWithProductsAndSuppliersById(2);
            assertNotNull(customer);
            assertEquals("远航设计工作室", customer.getCustomerName());
            assertEquals(2, customer.getProducts().size());
            assertEquals(2, customer.getSuppliers().size());
            assertEquals("无线鼠标", customer.getProducts().get(0).getProductName());
            assertTrue(customer.getProducts().get(0).getSold());
            assertFalse(customer.getProducts().get(0).getServiceAccepted());

            assertEquals(3, mapper.findAllWithProductsAndSuppliers().size());
        }
    }
}
