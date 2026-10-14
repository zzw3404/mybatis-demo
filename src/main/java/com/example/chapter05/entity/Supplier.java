package com.example.chapter05.entity;

import java.util.ArrayList;
import java.util.List;

/** Supplier and the products it is registered to supply. */
public class Supplier {
    private Integer supplierId;
    private String supplierName;
    private String contactName;
    private String phone;
    private String address;
    private List<Product> products = new ArrayList<>();

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
}
