package com.myhotel.repository;

import com.myhotel.entity.Room;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room,Long> {


    List<Room> findByHotelId(Long hotelId);

    List<Room> findByHotelIdAndAvailable(Long hotelId, boolean b);

    List<Room> findByHotelIdAndRoomType(Long hotelId, String roomType);

    List<Room> findAvailableRooms(Long hotelId, Integer capacity, BigDecimal maxPrice);

    boolean existsByRoomNumberAndHotelId(@NotBlank(message = "Room number is required") @Size(max = 10, message = "Room number must not exceed 10 characters") String roomNumber, Long hotelId);
}
