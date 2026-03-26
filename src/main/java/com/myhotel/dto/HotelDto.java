package com.myhotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelDto {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private String phoneNumber;
    private String email;
    private BigDecimal rating;
    private String description;
    private List<RoomDto> rooms;
}
