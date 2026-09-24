package com.example.chapter05;

import com.example.chapter05.entity.PurchaseDetail;
import com.example.chapter05.mapper.Chapter05BusinessMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

/** Validates the supplier/product/customer relationships described in the supplied screenshot. */
public class Chapter05BusinessMapperTest {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws Exception {
        try (InputStream inputStream = Resources.getResourceAsStream("chapter05/mybatis-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        }
    }

    @Test
    public void testSupplierAndProductManyToMany() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Chapter05BusinessMapper mapper = session.getMapper(Chapter05BusinessMapper.class);
            assertEquals(3, mapper.findProductsBySupplier(1).size());
            assertEquals(3, mapper.findSuppliersByProduct(2).size());
        }
    }

    @Test
    public void testCustomerPurchasesIdentifySupplierAndProduct() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Chapter05BusinessMapper mapper = session.getMapper(Chapter05BusinessMapper.class);
            List<PurchaseDetail> purchases = mapper.findPurchasesByCustomer(1);
            assertEquals(2, purchases.size());
            assertEquals("星河科技有限公司", purchases.get(0).getCustomerName());
            assertEquals("华东电子供应商", purchases.get(0).getSupplierName());
            assertEquals("商务笔记本电脑", purchases.get(0).getProductName());
        }
    }

    @Test
    public void testSupplierSalesAndProductServiceHistory() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Chapter05BusinessMapper mapper = session.getMapper(Chapter05BusinessMapper.class);
            assertEquals(2, mapper.findSalesBySupplier(1).size());
            List<PurchaseDetail> serviceRecords = mapper.findServiceRecordsByProduct(1);
            assertEquals(1, serviceRecords.size());
            assertEquals("安装调试", serviceRecords.get(0).getServiceType());
        }
    }
}
