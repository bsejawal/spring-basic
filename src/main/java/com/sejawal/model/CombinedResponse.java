package com.sejawal.model;

import com.sejawal.model.cart.Cart;
import com.sejawal.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CombinedResponse {
    private List<User> users;
    private List<Cart> carts;
}
