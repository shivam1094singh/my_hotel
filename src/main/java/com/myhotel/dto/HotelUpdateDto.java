package com.myhotel.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelUpdateDto {
    @Size(min=2 ,max=200,message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(min=2 ,max=50,message = "Address must be between 2 and 100 characters")
    private String address;

    @Size(min=2 ,max=50,message = "city must be between 2 and 50 characters")
    private String city;

    @Size(min=2 ,max=50,message = "country must be between 2 and 50 characters")
    private String country;

    @Size(min=2 ,max=50,message = "phone number must not exceed 20 characters")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    @DecimalMin(value = "0.0",message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0",message = "Rating must not exceed 5.0")
    private BigDecimal rating;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

}
