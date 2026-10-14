package com.example.chapter05.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Product with its suppliers and customers who have purchased it. */
public class Product {
    private Integer productId;
    private String productName;
    private String category;
    private BigDecimal unitPrice;
    /** True when at least one purchase exists for this product. */
    private Boolean sold;
    /** True when an after-sales service record exists for one of its purchases. */
    private Boolean serviceAccepted;
    private List<Supplier> suppliers = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Boolean getSold() { return sold; }
    public void setSold(Boolean sold) { this.sold = sold; }
    public Boolean getServiceAccepted() { return serviceAccepted; }
    public void setServiceAccepted(Boolean serviceAccepted) { this.serviceAccepted = serviceAccepted; }
    public List<Supplier> getSuppliers() { return suppliers; }
    public void setSuppliers(List<Supplier> suppliers) { this.suppliers = suppliers; }
    public List<Customer> getCustomers() { return customers; }
    public void setCustomers(List<Customer> customers) { this.customers = customers; }
}
