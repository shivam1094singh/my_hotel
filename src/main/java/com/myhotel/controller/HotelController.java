package com.myhotel.controller;

import com.myhotel.dto.HotelDto;
import com.myhotel.dto.HotelUpdateDto;
import com.myhotel.entity.Hotel;
import com.myhotel.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<HotelDto> createHotel(@Valid @RequestBody HotelUpdateDto hotelDto) {
        log.info("Creating new hotel with name: {}", hotelDto.getName());
        HotelDto createdHotel = hotelService.createHotel(hotelDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHotel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long id) {
        log.info("Getting hotel by id: {}", id);
        Hotel hotel = hotelService.getHotelById(id);
        HotelDto hotelDto = hotelService.convertToDto(hotel);
        return ResponseEntity.ok(hotelDto);
    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels() {
        log.info("Getting all hotels");
        List<HotelDto> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<HotelDto>> getHotelsByCity(@PathVariable String city) {
        log.info("Getting hotels by city: {}", city);
        List<HotelDto> hotels = hotelService.getHotelsByCity(city);
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<HotelDto>> getHotelsByCountry(@PathVariable String country) {
        log.info("Getting hotels by country: {}", country);
        List<HotelDto> hotels = hotelService.getHotelsByCountry(country);
        return ResponseEntity.ok(hotels);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelDto> updateHotel(@PathVariable Long id, @Valid @RequestBody HotelUpdateDto updateDto) {
        log.info("Updating hotel with id: {}", id);
        HotelDto updatedHotel = hotelService.updateHotel(id, updateDto);
        return ResponseEntity.ok(updatedHotel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHotel(@PathVariable Long id) {
        log.info("Deleting hotel with id: {}", id);
        hotelService.deleteHotel(id);
        return ResponseEntity.ok("Hotel deleted successfully");
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Hotel API is working!");
    }
}