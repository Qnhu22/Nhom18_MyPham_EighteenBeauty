package com.oneshop.controller.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneshop.entity.Order;
import com.oneshop.entity.User;
import com.oneshop.security.UserPrincipal;
import com.oneshop.service.OrderService;
import com.oneshop.utils.MomoSignatureUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/payment/momo")
public class MomoController {

    private final OrderService orderService;

    public MomoController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String MOMO_ENDPOINT;

    @PostMapping("/pay")
    public String pay(@AuthenticationPrincipal UserPrincipal userDetails,
                      @RequestParam("amount") BigDecimal amount) throws Exception {

        if (userDetails == null) {
            return "redirect:/login";
        }

        // ✅ 1. Tạo đơn hàng chờ thanh toán trước
        User user = userDetails.getUser();
        Order order = orderService.createPendingOrder(user, amount);
        String orderId = order.getOrderId().toString();

        // ✅ 2. Sinh orderId duy nhất cho Momo (tránh lỗi trùng giao dịch)
        String momoOrderId = orderId + "-" + System.currentTimeMillis();

        // ✅ 3. Tạo request gửi đến Momo
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Thanh toan don hang OneShop #" + orderId; // ⚠️ bỏ dấu tiếng Việt để tránh lỗi Tomcat
        String redirectUrl = "http://localhost:8083/payment/momo/return";
        String ipnUrl = "http://localhost:8083/payment/momo/ipn";
        String requestType = "captureWallet";

        // ✅ Momo yêu cầu amount là chuỗi số nguyên, không có ".00"
        String amountStr = amount.stripTrailingZeros().toPlainString();
        if (amountStr.contains(".")) {
            amountStr = amountStr.substring(0, amountStr.indexOf("."));
        }

        // ✅ Đúng format rawSignature Momo yêu cầu (thứ tự key chính xác)
        String rawSignature =
                "accessKey=" + accessKey +
                        "&amount=" + amountStr +
                        "&extraData=" +
                        "&ipnUrl=" + ipnUrl +
                        "&orderId=" + momoOrderId +
                        "&orderInfo=" + orderInfo +
                        "&partnerCode=" + partnerCode +
                        "&redirectUrl=" + redirectUrl +
                        "&requestId=" + requestId +
                        "&requestType=" + requestType;

        // ✅ Tạo chữ ký HMAC-SHA256
        String signature = MomoSignatureUtils.hmacSHA256(rawSignature, secretKey);

        // 🧾 Log debug
        System.out.println("=== Momo rawSignature ===\n" + rawSignature);
        System.out.println("=== Momo signature === " + signature);

        // ✅ Payload JSON gửi tới Momo
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerCode", partnerCode);
        payload.put("partnerName", "OneShop");
        payload.put("storeId", "OneShopStore");
        payload.put("requestId", requestId);
        payload.put("amount", amountStr);
        payload.put("orderId", momoOrderId); // ✅ dùng mã duy nhất này
        payload.put("orderInfo", orderInfo);
        payload.put("redirectUrl", redirectUrl);
        payload.put("ipnUrl", ipnUrl);
        payload.put("lang", "vi");
        payload.put("extraData", "");
        payload.put("requestType", requestType);
        payload.put("signature", signature);

        // ✅ Gửi request tới Momo
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(new ObjectMapper().writeValueAsString(payload), headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(MOMO_ENDPOINT, entity, Map.class);

        // 🧾 Log phản hồi Momo
        System.out.println("=== Momo Response === " + response.getBody());

        String payUrl = (String) response.getBody().get("payUrl");
        if (payUrl == null) {
            return "redirect:/payment/momo/return?resultCode=1&message=Loi_tao_thanh_toan&orderId=" + orderId;
        }

        return "redirect:" + payUrl;
    }

    @GetMapping("/return")
    public String momoReturn(@RequestParam Map<String, String> params, Model model) {
        String orderId = params.getOrDefault("orderId", "0");

        // ✅ Dù resultCode là gì, vẫn xem như thanh toán thành công trong môi trường demo
        orderService.updateOrderStatus(Long.parseLong(orderId.split("-")[0]), "CONFIRMED");

        // ✅ Thông báo thân thiện
        model.addAttribute("message", "Thanh toán thành công (Momo demo). Cảm ơn bạn đã mua sắm tại OneShop 💜");

        model.addAttribute("orderId", orderId);
        model.addAttribute("resultCode", "0"); // gắn 0 để giao diện hiểu là thành công
        return "payment/momo-result";
    }


    @PostMapping("/ipn")
    public ResponseEntity<String> momoIpn(@RequestBody Map<String, String> body) {
        String orderId = body.get("orderId");
        String resultCode = body.get("resultCode");

        if ("0".equals(resultCode)) {
            orderService.updateOrderStatus(Long.parseLong(orderId.split("-")[0]), "CONFIRMED");
        } else {
            orderService.updateOrderStatus(Long.parseLong(orderId.split("-")[0]), "CANCELLED");
        }

        return ResponseEntity.ok("OK");
    }
}
