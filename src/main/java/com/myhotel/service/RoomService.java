package com.myhotel.service;


import com.myhotel.dto.RoomDto;
import com.myhotel.dto.RoomUpdateDto;
import com.myhotel.entity.Room;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

public interface RoomService {
    RoomDto createRoom(Long hotelId, @Valid RoomUpdateDto roomDto);

    void deleteRoom(Long id);

    List<RoomDto> getRoomsByHotelIdAndType(Long hotelId, String roomType);

    List<RoomDto> getAvailableRoomsByHotelId(Long hotelId);

    List<RoomDto> getRoomsByHotelId(Long hotelId);

    List<RoomDto> findAvailableRooms(Long hotelId, Integer capacity, BigDecimal maxPrice);

    RoomDto updateRoom(Long id, @Valid RoomUpdateDto updateDto);

    void toggleRoomAvailability(Long id);

    List<RoomDto> getAllRooms();

    RoomDto convertToDto(Room room);

    Room getRoomById(Long id);
}
