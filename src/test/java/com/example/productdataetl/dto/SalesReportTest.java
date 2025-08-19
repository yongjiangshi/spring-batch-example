package com.example.productdataetl.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SalesReport DTO.
 */
class SalesReportTest {

    @Test
    void testSalesReportCreation() {
        // Given
        Long productId = 1L;
        String productName = "Laptop";
        BigDecimal price = new BigDecimal("999.99");

        // When
        SalesReport salesReport = new SalesReport(productId, productName, price);

        // Then
        assertEquals(productId, salesReport.getProductId());
        assertEquals(productName, salesReport.getProductName());
        assertEquals(price, salesReport.getPrice());
    }

    @Test
    void testSalesReportDefaultConstructor() {
        // When
        SalesReport salesReport = new SalesReport();

        // Then
        assertNull(salesReport.getProductId());
        assertNull(salesReport.getProductName());
        assertNull(salesReport.getPrice());
    }

    @Test
    void testSalesReportGettersAndSetters() {
        // Given
        SalesReport salesReport = new SalesReport();
        Long productId = 1L;
        String productName = "Test Product";
        BigDecimal price = new BigDecimal("100.00");

        // When
        salesReport.setProductId(productId);
        salesReport.setProductName(productName);
        salesReport.setPrice(price);

        // Then
        assertEquals(productId, salesReport.getProductId());
        assertEquals(productName, salesReport.getProductName());
        assertEquals(price, salesReport.getPrice());
    }

    @Test
    void testSalesReportEqualsAndHashCode() {
        // Given
        SalesReport report1 = new SalesReport(1L, "Laptop", new BigDecimal("999.99"));
        SalesReport report2 = new SalesReport(1L, "Laptop", new BigDecimal("999.99"));
        SalesReport report3 = new SalesReport(2L, "Mouse", new BigDecimal("25.50"));

        // Then
        assertEquals(report1, report2, "SalesReports with same data should be equal");
        assertNotEquals(report1, report3, "SalesReports with different data should not be equal");
        assertEquals(report1.hashCode(), report2.hashCode(), "Equal SalesReports should have same hash code");
        assertNotEquals(report1.hashCode(), report3.hashCode(), "Different SalesReports should have different hash codes");
    }

    @Test
    void testSalesReportEqualsWithNullValues() {
        // Given
        SalesReport report1 = new SalesReport(null, null, null);
        SalesReport report2 = new SalesReport(null, null, null);
        SalesReport report3 = new SalesReport(1L, "Product", new BigDecimal("100.00"));

        // Then
        assertEquals(report1, report2, "SalesReports with same null values should be equal");
        assertNotEquals(report1, report3, "SalesReport with nulls should not equal report with values");
    }

    @Test
    void testSalesReportEqualsWithPartialNullValues() {
        // Given
        SalesReport report1 = new SalesReport(1L, null, new BigDecimal("100.00"));
        SalesReport report2 = new SalesReport(1L, null, new BigDecimal("100.00"));
        SalesReport report3 = new SalesReport(1L, "Product", new BigDecimal("100.00"));

        // Then
        assertEquals(report1, report2, "SalesReports with same partial null values should be equal");
        assertNotEquals(report1, report3, "SalesReports with different null patterns should not be equal");
    }

    @Test
    void testSalesReportToString() {
        // Given
        SalesReport salesReport = new SalesReport(1L, "Laptop", new BigDecimal("999.99"));

        // When
        String toString = salesReport.toString();

        // Then
        assertTrue(toString.contains("productId=1"));
        assertTrue(toString.contains("productName='Laptop'"));
        assertTrue(toString.contains("price=999.99"));
    }

    @Test
    void testSalesReportToStringWithNullValues() {
        // Given
        SalesReport salesReport = new SalesReport(null, null, null);

        // When
        String toString = salesReport.toString();

        // Then
        assertTrue(toString.contains("productId=null"));
        assertTrue(toString.contains("productName='null'"));
        assertTrue(toString.contains("price=null"));
    }

    @Test
    void testSalesReportEqualsWithSameObject() {
        // Given
        SalesReport salesReport = new SalesReport(1L, "Laptop", new BigDecimal("999.99"));

        // Then
        assertEquals(salesReport, salesReport, "SalesReport should equal itself");
    }

    @Test
    void testSalesReportEqualsWithNull() {
        // Given
        SalesReport salesReport = new SalesReport(1L, "Laptop", new BigDecimal("999.99"));

        // Then
        assertNotEquals(salesReport, null, "SalesReport should not equal null");
    }

    @Test
    void testSalesReportEqualsWithDifferentClass() {
        // Given
        SalesReport salesReport = new SalesReport(1L, "Laptop", new BigDecimal("999.99"));
        String differentObject = "Not a SalesReport";

        // Then
        assertNotEquals(salesReport, differentObject, "SalesReport should not equal object of different class");
    }
}