package com.example.chapter05.mapper;

import com.example.chapter05.entity.Supplier;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** XML result-map examples for the supplier-to-product many-to-many relationship. */
public interface Chapter05SupplierMapper {
    List<Supplier> findAllWithProducts();
    Supplier findWithProductsById(@Param("supplierId") Integer supplierId);
}
