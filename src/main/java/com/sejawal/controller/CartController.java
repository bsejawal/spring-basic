package com.sejawal.controller;

import com.sejawal.model.ListCart;
import com.sejawal.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/carts")
    public ResponseEntity<ListCart> getData(){
        ListCart carts = cartService.getAllCart();
        return ResponseEntity.ok(carts);
    }
}
