package com.oneshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/vn")
public class AddressProxyController {

    private final RestTemplate rest = new RestTemplate();
    private static final String BASE_URL = "https://provinces.open-api.vn/api";

    @GetMapping("/p")
    public ResponseEntity<String> getProvinces() {
        String result = rest.getForObject(BASE_URL + "/p/", String.class); // thêm "/"
        return ResponseEntity.ok(result);
    }

    // ✅ Lấy chi tiết Tỉnh/Thành (để có danh sách Quận/Huyện)
    @GetMapping("/p-detail")
    public ResponseEntity<String> getProvinceDetail(@RequestParam String code) {
        String result = rest.getForObject(BASE_URL + "/p/" + code + "?depth=2", String.class);
        return ResponseEntity.ok(result);
    }

    // ✅ Lấy chi tiết Quận/Huyện (để có danh sách Phường/Xã)
    @GetMapping("/d-detail")
    public ResponseEntity<String> getDistrictDetail(@RequestParam String code) {
        String result = rest.getForObject(BASE_URL + "/d/" + code + "?depth=2", String.class);
        return ResponseEntity.ok(result);
    }
}
