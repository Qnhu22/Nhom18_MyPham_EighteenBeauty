package com.oneshop.dto;

import com.oneshop.entity.OrderAddress;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddressDTO {
    private Long addressId;
    private String receiverName;
    private String phone;
    private String addressLine;
    private String ward;
    private String district;
    private String city;
    private boolean defaultAddress;

    public static OrderAddressDTO fromEntity(OrderAddress address) {
        return OrderAddressDTO.builder()
                .addressId(address.getAddressId())
                .receiverName(address.getReceiverName())
                .phone(address.getPhone())
                .addressLine(address.getAddressLine())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .defaultAddress(address.isDefaultAddress())
                .build();
    }
}
