package com.myhotel.repository;

import com.myhotel.entity.Booking;
import com.myhotel.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByHotelId(Long hotelId);

    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByStatus(BookingStatus status);

    Optional<Booking> findByBookingReference(String bookingReference);

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND " +
           "((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate) OR " +
           "(b.checkInDate >= :checkInDate AND b.checkInDate <= :checkOutDate)) " +
           "AND b.status NOT IN ('CANCELLED')")
    List<Booking> findConflictingBookings(@Param("roomId") Long roomId,
                                         @Param("checkInDate") LocalDate checkInDate,
                                         @Param("checkOutDate") LocalDate checkOutDate);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = :status")
    List<Booking> findByUserIdAndStatus(@Param("userId") Long userId, 
                                       @Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.id = :roomId AND " +
           "b.checkInDate <= :date AND b.checkOutDate >= :date AND " +
           "b.status NOT IN ('CANCELLED')")
    long countActiveBookingsForRoomOnDate(@Param("roomId") Long roomId, @Param("date") LocalDate date);

    boolean existsByBookingReference(String bookingReference);
}
