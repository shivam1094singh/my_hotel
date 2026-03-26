package com.myhotel.dto;

import com.myhotel.entity.Booking;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingUpdateDto {

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    @Min(value = 1, message = "At least 1 guest is required")
    @Max(value = 10, message = "Maximum 10 guests allowed")
    private Integer numberOfGuests;

    private String specialRequests;

    private Booking status;
}