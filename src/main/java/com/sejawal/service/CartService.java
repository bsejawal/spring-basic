package com.sejawal.service;


import com.sejawal.model.CombinedResponse;
import com.sejawal.model.cart.Cart;
import com.sejawal.model.cart.CartsWrapper;
import com.sejawal.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final WebClient webClient;

    private static final String CART_URL = "https://dummyjson.com/carts";
    private static final String USER_URL = "https://jsonplaceholder.typicode.com/users";


    public Mono<CombinedResponse> getAllCart() {
        Mono<List<Cart>> cartsMono = webClient
                .get()
                .uri(CART_URL)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new RuntimeException("Client error when fetching carts: " + response.statusCode())))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new RuntimeException("Server error when fetching carts: " + response.statusCode())))
                .bodyToMono(CartsWrapper.class)
                .map(CartsWrapper::getCarts)
                .onErrorResume(ex ->{
                                System.out.println("Error fetching carts: "+ ex.getMessage());
                                return Mono.just(Collections.emptyList());
                });

        Mono<List<User>> usersMono = webClient.get()
                .uri(USER_URL)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new RuntimeException("Client error when fetching users :" + response.statusCode())))
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new RuntimeException("Client error when fetching users :"+ response.statusCode())))
                .bodyToFlux(User.class)
                .collectList()
                .onErrorResume(ex -> {
                    System.out.println("Error fetching users: "+ ex.getMessage());
                    return Mono.just(Collections.emptyList());
                });

        return Mono.zip(usersMono, cartsMono)
                .map(t -> new CombinedResponse(t.getT1(), t.getT2()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    System.err.println("Global WebClient error: "+ ex.getMessage());
                    return Mono.just(new CombinedResponse(Collections.emptyList(), Collections.emptyList()));
                })
                .onErrorResume(Exception.class, ex -> {
                    System.out.println("unexpected error: "+ ex.getMessage());
                    return Mono.just(new CombinedResponse(Collections.emptyList(), Collections.emptyList()));
                });
    }
}
