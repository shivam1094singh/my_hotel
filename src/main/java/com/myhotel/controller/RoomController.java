package com.myhotel.controller;

import com.myhotel.dto.RoomDto;
import com.myhotel.dto.RoomUpdateDto;
import com.myhotel.entity.Room;
import com.myhotel.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/hotel/{hotelId}")
    public ResponseEntity<RoomDto> createRoom(@PathVariable Long hotelId,
                                              @Valid @RequestBody RoomUpdateDto roomDto) {
        log.info("Creating new room with number: {} for hotel: {}", roomDto.getRoomNumber(), hotelId);
        RoomDto createdRoom = roomService.createRoom(hotelId, roomDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id) {
        log.info("Getting room by id: {}", id);
        Room room = roomService.getRoomById(id);
        RoomDto roomDto = roomService.convertToDto(room);
        return ResponseEntity.ok(roomDto);
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRooms() {
        log.info("Getting all rooms");
        List<RoomDto> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<RoomDto>> getRoomsByHotelId(@PathVariable Long hotelId) {
        log.info("Getting rooms for hotel: {}", hotelId);
        List<RoomDto> rooms = roomService.getRoomsByHotelId(hotelId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/hotel/{hotelId}/available")
    public ResponseEntity<List<RoomDto>> getAvailableRoomsByHotelId(@PathVariable Long hotelId) {
        log.info("Getting available rooms for hotel: {}", hotelId);
        List<RoomDto> rooms = roomService.getAvailableRoomsByHotelId(hotelId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/hotel/{hotelId}/type/{roomType}")
    public ResponseEntity<List<RoomDto>> getRoomsByHotelIdAndType(@PathVariable Long hotelId,
                                                                  @PathVariable String roomType) {
        log.info("Getting {} rooms for hotel: {}", roomType, hotelId);
        List<RoomDto> rooms = roomService.getRoomsByHotelIdAndType(hotelId, roomType);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RoomDto>> findAvailableRooms(@RequestParam Long hotelId,
                                                            @RequestParam Integer capacity,
                                                            @RequestParam BigDecimal maxPrice) {
        log.info("Finding available rooms for hotel: {}, capacity: {}, max price: {}",
                hotelId, capacity, maxPrice);
        List<RoomDto> rooms = roomService.findAvailableRooms(hotelId, capacity, maxPrice);
        return ResponseEntity.ok(rooms);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomDto> updateRoom(@PathVariable Long id,
                                              @Valid @RequestBody RoomUpdateDto updateDto) {
        log.info("Updating room with id: {}", id);
        RoomDto updatedRoom = roomService.updateRoom(id, updateDto);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoom(@PathVariable Long id) {
        log.info("Deleting room with id: {}", id);
        roomService.deleteRoom(id);
        return ResponseEntity.ok("Room deleted successfully");
    }

    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<String> toggleRoomAvailability(@PathVariable Long id) {
        log.info("Toggling availability for room with id: {}", id);
        roomService.toggleRoomAvailability(id);
        return ResponseEntity.ok("Room availability toggled successfully");
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Room API is working!");
    }
}