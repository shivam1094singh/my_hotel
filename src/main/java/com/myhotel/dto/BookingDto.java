package com.myhotel.dto;

import com.myhotel.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long id;
    private Long userId;
    private String userName;
    private Long hotelId;
    private String hotelName;
    private Long roomId;
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
    private Integer numberOfGuests;
    private String specialRequests;
    private Booking status;
    private LocalDate bookingDate;
    private String bookingReference;
}
