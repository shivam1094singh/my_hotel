package com.myhotel.service;

import com.myhotel.dto.BookingDto;
import com.myhotel.dto.BookingCreateDto;
import com.myhotel.dto.BookingUpdateDto;
import com.myhotel.entity.Booking;
import com.myhotel.entity.Hotel;
import com.myhotel.entity.Room;
import com.myhotel.entity.User;
import com.myhotel.entity.enums.BookingStatus;
import com.myhotel.repository.BookingRepository;
import com.myhotel.repository.HotelRepository;
import com.myhotel.repository.RoomRepository;
import com.myhotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImp implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    @Override
    public BookingDto convertToDto(Booking booking) {
        BookingDto dto = modelMapper.map(booking, BookingDto.class);
        dto.setUserId(booking.getUser().getId());
        dto.setUserName(booking.getUser().getName());
        dto.setHotelId(booking.getHotel().getId());
        dto.setHotelName(booking.getHotel().getName());
        dto.setRoomId(booking.getRoom().getId());
        dto.setRoomNumber(booking.getRoom().getRoomNumber());
        return dto;
    }

    @Override
    public List<BookingDto> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsByHotelId(Long hotelId) {
        List<Booking> bookings = bookingRepository.findByHotelId(hotelId);
        return bookings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsByStatus(BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByStatus(status);
        return bookings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingDto createBooking(BookingCreateDto bookingDto) {
        User user = userRepository.findById(bookingDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + bookingDto.getUserId()));

        Room room = roomRepository.findById(bookingDto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + bookingDto.getRoomId()));

        Hotel hotel = room.getHotel();

        // Validate dates
        if (bookingDto.getCheckInDate().isAfter(bookingDto.getCheckOutDate())) {
            throw new RuntimeException("Check-in date must be before check-out date");
        }

        if (bookingDto.getCheckInDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Check-in date cannot be in the past");
        }

        // Check room availability
        if (!isRoomAvailable(room.getId(), bookingDto.getCheckInDate(), bookingDto.getCheckOutDate())) {
            throw new RuntimeException("Room is not available for the selected dates");
        }

        // Validate capacity
        if (bookingDto.getNumberOfGuests() > room.getCapacity()) {
            throw new RuntimeException("Room capacity exceeded");
        }

        // Calculate total price
        long nights = ChronoUnit.DAYS.between(bookingDto.getCheckInDate(), bookingDto.getCheckOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setHotel(hotel);
        booking.setRoom(room);
        booking.setCheckInDate(bookingDto.getCheckInDate());
        booking.setCheckOutDate(bookingDto.getCheckOutDate());
        booking.setTotalPrice(totalPrice);
        booking.setNumberOfGuests(bookingDto.getNumberOfGuests());
        booking.setSpecialRequests(bookingDto.getSpecialRequests());
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingDate(LocalDate.now());
        booking.setBookingReference(generateBookingReference());

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with reference: {}", savedBooking.getBookingReference());
        return convertToDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto updateBooking(Long id, BookingUpdateDto updateDto) {
        Booking booking = getBookingById(id);

        if (updateDto.getCheckInDate() != null || updateDto.getCheckOutDate() != null) {
            LocalDate newCheckIn = updateDto.getCheckInDate() != null ? updateDto.getCheckInDate() : booking.getCheckInDate();
            LocalDate newCheckOut = updateDto.getCheckOutDate() != null ? updateDto.getCheckOutDate() : booking.getCheckOutDate();

            if (newCheckIn.isAfter(newCheckOut)) {
                throw new RuntimeException("Check-in date must be before check-out date");
            }

            if (!isRoomAvailable(booking.getRoom().getId(), newCheckIn, newCheckOut)) {
                throw new RuntimeException("Room is not available for the selected dates");
            }

            booking.setCheckInDate(newCheckIn);
            booking.setCheckOutDate(newCheckOut);

            // Recalculate price
            long nights = ChronoUnit.DAYS.between(newCheckIn, newCheckOut);
            BigDecimal totalPrice = booking.getRoom().getPricePerNight().multiply(BigDecimal.valueOf(nights));
            booking.setTotalPrice(totalPrice);
        }

        if (updateDto.getNumberOfGuests() != null) {
            if (updateDto.getNumberOfGuests() > booking.getRoom().getCapacity()) {
                throw new RuntimeException("Room capacity exceeded");
            }
            booking.setNumberOfGuests(updateDto.getNumberOfGuests());
        }

        if (updateDto.getSpecialRequests() != null) {
            booking.setSpecialRequests(updateDto.getSpecialRequests());
        }

        if (updateDto.getStatus() != null) {
            booking.setStatus(updateDto.getStatus());
        }

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking updated with reference: {}", updatedBooking.getBookingReference());
        return convertToDto(updatedBooking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = getBookingById(id);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking cancelled with reference: {}", booking.getBookingReference());
    }

    @Override
    @Transactional
    public void confirmBooking(Long id) {
        Booking booking = getBookingById(id);
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        log.info("Booking confirmed with reference: {}", booking.getBookingReference());
    }

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                roomId, checkInDate, checkOutDate);
        return conflictingBookings.isEmpty();
    }

    @Override
    public String generateBookingReference() {
        String reference;
        do {
            reference = "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (bookingRepository.existsByBookingReference(reference));
        return reference;
    }
}