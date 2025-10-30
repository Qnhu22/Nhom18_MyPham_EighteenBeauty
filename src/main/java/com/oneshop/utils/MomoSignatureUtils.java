package com.oneshop.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MomoSignatureUtils {
    public static String hmacSHA256(String data, String secretKey) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        hmac.init(secretKeySpec);
        byte[] bytes = hmac.doFinal(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
