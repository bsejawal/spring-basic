package com.sejawal.model.cart;

import lombok.Data;

import java.util.List;

@Data
public class CartsWrapper {
    private List<Cart> carts;
    private int total;
    private int skip;
    private int limit;

}
