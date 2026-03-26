package com.myhotel.controller;

import com.myhotel.dto.BookingDto;
import com.myhotel.dto.BookingCreateDto;
import com.myhotel.dto.BookingUpdateDto;
import com.myhotel.entity.Booking;
import com.myhotel.entity.enums.BookingStatus;
import com.myhotel.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingCreateDto bookingDto) {
        log.info("Creating new booking for user: {}, room: {}", 
                bookingDto.getUserId(), bookingDto.getRoomId());
        BookingDto createdBooking = bookingService.createBooking(bookingDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long id) {
        log.info("Getting booking by id: {}", id);
        Booking booking = bookingService.getBookingById(id);
        BookingDto bookingDto = bookingService.convertToDto(booking);
        return ResponseEntity.ok(bookingDto);
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        log.info("Getting all bookings");
        List<BookingDto> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDto>> getBookingsByUserId(@PathVariable Long userId) {
        log.info("Getting bookings for user: {}", userId);
        List<BookingDto> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<BookingDto>> getBookingsByHotelId(@PathVariable Long hotelId) {
        log.info("Getting bookings for hotel: {}", hotelId);
        List<BookingDto> bookings = bookingService.getBookingsByHotelId(hotelId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingDto>> getBookingsByStatus(@PathVariable BookingStatus status) {
        log.info("Getting bookings with status: {}", status);
        List<BookingDto> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDto> updateBooking(@PathVariable Long id, 
                                                   @Valid @RequestBody BookingUpdateDto updateDto) {
        log.info("Updating booking with id: {}", id);
        BookingDto updatedBooking = bookingService.updateBooking(id, updateDto);
        return ResponseEntity.ok(updatedBooking);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        log.info("Cancelling booking with id: {}", id);
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled successfully");
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<String> confirmBooking(@PathVariable Long id) {
        log.info("Confirming booking with id: {}", id);
        bookingService.confirmBooking(id);
        return ResponseEntity.ok("Booking confirmed successfully");
    }

    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> checkRoomAvailability(@RequestParam Long roomId,
                                                       @RequestParam String checkInDate,
                                                       @RequestParam String checkOutDate) {
        LocalDate checkIn = LocalDate.parse(checkInDate);
        LocalDate checkOut = LocalDate.parse(checkOutDate);
        boolean available = bookingService.isRoomAvailable(roomId, checkIn, checkOut);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Booking API is working!");
    }
}
