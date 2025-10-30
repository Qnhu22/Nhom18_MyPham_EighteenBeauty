package com.oneshop.controller.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneshop.utils.MomoSignatureUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.ui.Model;

import java.util.*;

@Controller
@RequestMapping("/payment/momo")
public class MomoController {

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String MOMO_ENDPOINT;

    @PostMapping("/pay")
    public String pay(@RequestParam("orderId") String orderId,
                      @RequestParam("amount") Long amount) throws Exception {

        // ⚙️ Chuẩn bị dữ liệu Momo
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Thanh toán đơn hàng OneShop #" + orderId;
        String redirectUrl = "http://localhost:8083/payment/momo/return";
        String ipnUrl = "http://localhost:8083/payment/momo/ipn";
        String requestType = "captureWallet";

        // 🔐 Tạo chữ ký
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = MomoSignatureUtils.hmacSHA256(rawSignature, secretKey);

        // 📦 Payload
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerCode", partnerCode);
        payload.put("partnerName", "OneShop");
        payload.put("storeId", "OneShopStore");
        payload.put("requestId", requestId);
        payload.put("amount", String.valueOf(amount));
        payload.put("orderId", orderId);
        payload.put("orderInfo", orderInfo);
        payload.put("redirectUrl", redirectUrl);
        payload.put("ipnUrl", ipnUrl);
        payload.put("lang", "vi");
        payload.put("extraData", "");
        payload.put("requestType", requestType);
        payload.put("signature", signature);

        // 🚀 Gửi request đến Momo thật
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(new ObjectMapper().writeValueAsString(payload), headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(MOMO_ENDPOINT, entity, Map.class);

        // ✅ Lấy link QR
        String payUrl = (String) response.getBody().get("payUrl");

        if (payUrl == null) {
            // Nếu có lỗi → cho về trang lỗi
            return "redirect:/payment/momo/return?resultCode=1&message=Không tạo được mã QR&orderId=" + orderId;
        }

        System.out.println("✅ Tạo thanh toán demo thành công, chuyển người dùng sang Momo: " + payUrl);

        // 👉 Vẫn redirect sang trang QR Momo thật
        return "redirect:" + payUrl;
    }

    @GetMapping("/return")
    public String momoReturn(@RequestParam Map<String, String> params, Model model) {
        String orderId = params.getOrDefault("orderId", "DEMO_ORDER");
        String resultCode = params.getOrDefault("resultCode", "0");

        // ⚙️ Nếu người dùng hủy (49), vẫn coi như demo thành công
        if (!"0".equals(resultCode)) {
            resultCode = "0";
        }

        model.addAttribute("orderId", orderId);
        model.addAttribute("resultCode", resultCode);
        model.addAttribute("message", "Thanh toán thành công (Mô phỏng demo)");
        return "payment/momo-result";
    }

}
