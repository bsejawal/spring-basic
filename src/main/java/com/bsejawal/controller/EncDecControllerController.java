package com.bsejawal.controller;

import com.bsejawal.service.EncDecService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.PartEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Reactive REST endpoints for file uploads. Consumes the request body as a
 * stream of {@link PartEvent} so file content is never fully buffered in memory
 * at the framework level — events flow straight through to S3 via the service.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class EncDecControllerController {
    @Autowired
    EncDecService encDecService;

    @PostMapping("/enc")
    public String encrypt(@RequestBody String data) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return encDecService.encrypt(data);
    }

    @PostMapping("/dec")
    public String decrypt(@RequestBody String encryptedData) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        return encDecService.decrypt(encryptedData);
    }

}
