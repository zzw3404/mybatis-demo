package com.example.chapter05.mapper;

import com.example.chapter05.entity.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** XML result-map examples for products, their suppliers and purchasing customers. */
public interface Chapter05ProductMapper {
    List<Product> findSoldProductsWithSuppliersAndCustomers();
    Product findWithSuppliersAndCustomersById(@Param("productId") Integer productId);
}
