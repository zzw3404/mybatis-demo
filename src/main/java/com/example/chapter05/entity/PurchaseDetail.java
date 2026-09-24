package com.example.chapter05.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 供应商、商品、客户及售后服务的查询结果投影。 */
public class PurchaseDetail {
    private Integer purchaseId;
    private String customerName;
    private String supplierName;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private LocalDateTime purchasedAt;
    private String serviceType;
    private String serviceNote;

    public Integer getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Integer purchaseId) { this.purchaseId = purchaseId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public LocalDateTime getPurchasedAt() { return purchasedAt; }
    public void setPurchasedAt(LocalDateTime purchasedAt) { this.purchasedAt = purchasedAt; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getServiceNote() { return serviceNote; }
    public void setServiceNote(String serviceNote) { this.serviceNote = serviceNote; }
}
