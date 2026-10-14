package com.example.chapter05.mapper;

import com.example.chapter05.entity.Customer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** XML result-map examples for customers and the products/suppliers in purchase history. */
public interface Chapter05CustomerMapper {
    List<Customer> findAllWithProductsAndSuppliers();
    Customer findWithProductsAndSuppliersById(@Param("customerId") Integer customerId);
}
