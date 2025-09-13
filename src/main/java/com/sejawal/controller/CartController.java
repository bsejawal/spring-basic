package com.sejawal.controller;

import com.sejawal.model.CombinedResponse;
import com.sejawal.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/combined")
    public Mono<CombinedResponse> getData(){
        Mono<CombinedResponse> combinedResponseMono = cartService.getAllCart();
        return combinedResponseMono;
    }
}
