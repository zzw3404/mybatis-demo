package com.example.chapter05.entity;

import java.util.ArrayList;
import java.util.List;

/** Customer with products purchased and the suppliers on those purchases. */
public class Customer {
    private Integer customerId;
    private String customerName;
    private String phone;
    private String address;
    private List<Product> products = new ArrayList<>();
    private List<Supplier> suppliers = new ArrayList<>();

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
    public List<Supplier> getSuppliers() { return suppliers; }
    public void setSuppliers(List<Supplier> suppliers) { this.suppliers = suppliers; }
}
