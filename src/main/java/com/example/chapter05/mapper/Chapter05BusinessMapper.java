package com.example.chapter05.mapper;

import com.example.chapter05.entity.PurchaseDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 截图业务关系：供应商供货、客户采购、商品售后服务查询。 */
public interface Chapter05BusinessMapper {
    List<String> findProductsBySupplier(@Param("supplierId") Integer supplierId);
    List<String> findSuppliersByProduct(@Param("productId") Integer productId);
    List<PurchaseDetail> findPurchasesByCustomer(@Param("customerId") Integer customerId);
    List<PurchaseDetail> findSalesBySupplier(@Param("supplierId") Integer supplierId);
    List<PurchaseDetail> findServiceRecordsByProduct(@Param("productId") Integer productId);
}
