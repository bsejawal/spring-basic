package com.sejawal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class CartController {

    @GetMapping("/carts")
    public ResponseEntity<String> getData(){
        return ResponseEntity.ok("OKKKKK");
    }
}
