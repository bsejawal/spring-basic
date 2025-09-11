package com.sejawal.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {
    private int id;
    private List<Product> products;
    private BigDecimal total;
    private BigDecimal discountedTotal;
    private int userId;
    private int totalProducts;
    private int totalQuantity;

}
