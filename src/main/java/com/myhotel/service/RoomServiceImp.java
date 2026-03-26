package com.myhotel.service;

import com.myhotel.dto.RoomDto;
import com.myhotel.dto.RoomUpdateDto;
import com.myhotel.entity.Hotel;
import com.myhotel.entity.Room;
import com.myhotel.repository.HotelRepository;
import com.myhotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImp implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    @Override
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }

    @Override
    public RoomDto convertToDto(Room room) {
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomDto> getRoomsByHotelId(Long hotelId) {
        List<Room> rooms = roomRepository.findByHotelId(hotelId);
        return rooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomDto> getAvailableRoomsByHotelId(Long hotelId) {
        List<Room> rooms = roomRepository.findByHotelIdAndAvailable(hotelId, true);
        return rooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomDto> getRoomsByHotelIdAndType(Long hotelId, String roomType) {
        List<Room> rooms = roomRepository.findByHotelIdAndRoomType(hotelId, roomType);
        return rooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomDto> findAvailableRooms(Long hotelId, Integer capacity, java.math.BigDecimal maxPrice) {
        List<Room> rooms = roomRepository.findAvailableRooms(hotelId, capacity, maxPrice);
        return rooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto createRoom(Long hotelId, RoomUpdateDto roomDto) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        if (roomRepository.existsByRoomNumberAndHotelId(roomDto.getRoomNumber(), hotelId)) {
            throw new RuntimeException("Room already exists with number: " + roomDto.getRoomNumber());
        }

        Room room = new Room();
        room.setRoomNumber(roomDto.getRoomNumber());
        room.setRoomType(roomDto.getRoomType());
        room.setPricePerNight(roomDto.getPricePerNight());
        room.setCapacity(roomDto.getCapacity());
        room.setAvailable(roomDto.getAvailable());
        room.setDescription(roomDto.getDescription());
        room.setHotel(hotel);

        Room savedRoom = roomRepository.save(room);
        log.info("Room created successfully with number: {} for hotel: {}",
                savedRoom.getRoomNumber(), hotel.getName());
        return convertToDto(savedRoom);
    }

    @Override
    public RoomDto updateRoom(Long id, RoomUpdateDto updateDto) {
        Room room = getRoomById(id);

        if (updateDto.getRoomNumber() != null && !updateDto.getRoomNumber().equals(room.getRoomNumber())) {
            if (roomRepository.existsByRoomNumberAndHotelId(updateDto.getRoomNumber(), room.getHotel().getId())) {
                throw new RuntimeException("Room already exists with number: " + updateDto.getRoomNumber());
            }
            room.setRoomNumber(updateDto.getRoomNumber());
        }

        if (updateDto.getRoomType() != null) {
            room.setRoomType(updateDto.getRoomType());
        }

        if (updateDto.getPricePerNight() != null) {
            room.setPricePerNight(updateDto.getPricePerNight());
        }

        if (updateDto.getCapacity() != null) {
            room.setCapacity(updateDto.getCapacity());
        }

        if (updateDto.getAvailable() != null) {
            room.setAvailable(updateDto.getAvailable());
        }

        if (updateDto.getDescription() != null) {
            room.setDescription(updateDto.getDescription());
        }

        Room updatedRoom = roomRepository.save(room);
        log.info("Room updated with number: {}", updatedRoom.getRoomNumber());
        return convertToDto(updatedRoom);
    }

    @Override
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
        log.info("Room deleted with id: {}", id);
    }

    @Override
    public void toggleRoomAvailability(Long id) {
        Room room = getRoomById(id);
        room.setAvailable(!room.getAvailable());
        roomRepository.save(room);
        log.info("Room availability toggled for room: {}, new status: {}",
                room.getRoomNumber(), room.getAvailable());
    }
}