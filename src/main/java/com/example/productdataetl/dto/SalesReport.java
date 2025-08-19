package com.example.productdataetl.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Data Transfer Object for Sales Report generation.
 * Used to represent filtered product data in the output CSV file.
 */
public class SalesReport {

    private Long productId;
    private String productName;
    private BigDecimal price;

    // Default constructor
    public SalesReport() {
    }

    // Constructor for creating SalesReport instances
    public SalesReport(Long productId, String productName, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesReport that = (SalesReport) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(productName, that.productName) &&
                Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, price);
    }

    @Override
    public String toString() {
        return "SalesReport{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", price=" + price +
                '}';
    }
}