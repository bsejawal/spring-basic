package com.sejawal.service;


import com.sejawal.model.ListCart;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
//@RequiredArgsConstructor
public class CartService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BASE_URL = "https://dummyjson.com/carts";


    public ListCart getAllCart() {
       ResponseEntity<ListCart> response = restTemplate.exchange(

               BASE_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ListCart>() {}
        );
        return response.getBody();
    }
}
