package com.sejawal.model.cart;

import lombok.Data;

@Data
public class Product {
    private int id;
    private String title;
    private double price;
    private int quantity;
    private double total;
    private double discountPercentage;
    private double discountedTotal;
    private String thumbnail;
}
