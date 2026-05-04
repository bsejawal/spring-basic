package com.bsejawal.service;

import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.Base64;

@Service
public class EncDecService {
    public static final String RSA = "RSA";
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private void init() throws NoSuchAlgorithmException{
        KeyPair keyPair = generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
        keyPairGenerator.initialize(2096);
        return keyPairGenerator.generateKeyPair();
    }

    public String encrypt(String data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //Generate Key Pair
        //Initialize the private key and public key
        init();
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedValue = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedValue);
    }

    public String decrypt(String encryptedData) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedValue = cipher.doFinal(Base64.getMimeDecoder().decode(encryptedData.getBytes()));
        return new String(decryptedValue);

    }
}
