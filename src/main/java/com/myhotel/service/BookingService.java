package com.myhotel.service;

import com.myhotel.dto.BookingDto;
import com.myhotel.dto.BookingCreateDto;
import com.myhotel.dto.BookingUpdateDto;
import com.myhotel.entity.Booking;
import com.myhotel.entity.enums.BookingStatus;

import java.util.List;

public interface BookingService {

    Booking getBookingById(Long id);

    BookingDto convertToDto(Booking booking);

    List<BookingDto> getAllBookings();

    List<BookingDto> getBookingsByUserId(Long userId);

    List<BookingDto> getBookingsByHotelId(Long hotelId);

    List<BookingDto> getBookingsByStatus(BookingStatus status);

    BookingDto createBooking(BookingCreateDto bookingDto);

    BookingDto updateBooking(Long id, BookingUpdateDto updateDto);

    void cancelBooking(Long id);

    void confirmBooking(Long id);

    boolean isRoomAvailable(Long roomId, java.time.LocalDate checkInDate,
                            java.time.LocalDate checkOutDate);

    String generateBookingReference();
}