package com.myhotel.controller;

import com.myhotel.dto.HotelDto;
import com.myhotel.dto.RoomDto;
import com.myhotel.dto.SearchCriteriaDto;
import com.myhotel.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * SearchController - REST API endpoints for hotel and room search
 * 
 * Controller Layer Pattern:
 * - Handles HTTP requests and responses
 * - Validates input data
 * - Calls appropriate service methods
 * - Returns JSON responses
 * - Maps URLs to business logic
 * 
 * Key Annotations:
 * - @RestController: Combines @Controller and @ResponseBody
 * - @RequestMapping: Base URL for all endpoints
 * - @RequiredArgsConstructor: Lombok for dependency injection
 * - @Slf4j: Lombok for logging
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    // Dependency injection - Spring injects SearchService implementation
    private final SearchService searchService;

    /**
     * Search hotels with complex criteria (POST method)
     * 
     * Use Case: Advanced search form with multiple filters
     * 
     * Flow:
     * 1. Client sends POST request with SearchCriteriaDto in body
     * 2. Spring automatically converts JSON to SearchCriteriaDto object
     * 3. Controller calls searchService.searchHotels()
     * 4. Service returns List<HotelDto>
     * 5. Spring automatically converts List<HotelDto> to JSON array
     * 6. Client receives JSON response
     * 
     * @param criteria - Search parameters from request body
     * @return List of hotels matching criteria
     */
    @PostMapping("/hotels")
    public ResponseEntity<List<HotelDto>> searchHotels(@RequestBody SearchCriteriaDto criteria) {
        log.info("Searching hotels with criteria: {}", criteria);
        
        // Delegate business logic to service layer
        List<HotelDto> hotels = searchService.searchHotels(criteria);
        
        // Return 200 OK with the hotel list
        return ResponseEntity.ok(hotels);
    }

    /**
     * Search hotels with query parameters (GET method)
     * 
     * Use Case: Simple search via URL parameters (good for bookmarking/sharing)
     * 
     * Example URL: /api/search/hotels?city=New York&minRating=4&maxPrice=200
     * 
     * Flow:
     * 1. Client sends GET request with query parameters
     * 2. Spring binds parameters to method arguments
     * 3. Controller creates SearchCriteriaDto from parameters
     * 4. Calls service to perform search
     * 5. Returns results as JSON
     * 
     * @param city - City to search in
     * @param country - Country to search in
     * @param nameKeyword - Hotel name keyword
     * @param minRating - Minimum hotel rating
     * @param maxRating - Maximum hotel rating
     * @param minPrice - Minimum room price
     * @param maxPrice - Maximum room price
     * @param capacity - Required room capacity
     * @param roomType - Room type filter
     * @param checkInDate - Check-in date (string format)
     * @param checkOutDate - Check-out date (string format)
     * @return List of hotels matching criteria
     */
    @GetMapping("/hotels")
    public ResponseEntity<List<HotelDto>> searchHotelsByParams(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal maxRating,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String checkInDate,
            @RequestParam(required = false) String checkOutDate) {

        // Create SearchCriteriaDto from individual parameters
        // This is the Data Transfer Object pattern in action
        SearchCriteriaDto criteria = new SearchCriteriaDto();
        criteria.setCity(city);
        criteria.setCountry(country);
        criteria.setNameKeyword(nameKeyword);
        criteria.setMinRating(minRating);
        criteria.setMaxRating(maxRating);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setCapacity(capacity);
        criteria.setRoomType(roomType);
        
        // Convert string dates to LocalDate objects
        // @RequestParam always gives strings, need to parse to dates
        if (checkInDate != null) {
            criteria.setCheckInDate(LocalDate.parse(checkInDate));
        }
        if (checkOutDate != null) {
            criteria.setCheckOutDate(LocalDate.parse(checkOutDate));
        }

        log.info("Searching hotels with parameters: {}", criteria);
        List<HotelDto> hotels = searchService.searchHotels(criteria);
        return ResponseEntity.ok(hotels);
    }

    /**
     * Search available rooms for a specific hotel (POST method)
     * 
     * Use Case: Room search within a hotel with complex criteria
     * 
     * Flow:
     * 1. Client wants to see rooms for a specific hotel
     * 2. Sends hotel ID in URL, search criteria in body
     * 3. Controller calls service to find available rooms
     * 4. Service checks room availability and filters
     * 5. Returns list of bookable rooms
     * 
     * @param hotelId - Hotel ID from URL path
     * @param criteria - Room search criteria from request body
     * @return List of available rooms
     */
    @PostMapping("/rooms/{hotelId}")
    public ResponseEntity<List<RoomDto>> searchAvailableRooms(@PathVariable Long hotelId, 
                                                             @RequestBody SearchCriteriaDto criteria) {
        log.info("Searching available rooms for hotel: {} with criteria: {}", hotelId, criteria);
        
        // @PathVariable extracts hotelId from URL: /api/search/rooms/{hotelId}
        List<RoomDto> rooms = searchService.searchAvailableRooms(hotelId, criteria);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Search available rooms with query parameters (GET method)
     * 
     * Use Case: Simple room search via URL parameters
     * 
     * Example URL: /api/search/rooms/123?capacity=2&maxPrice=150
     * 
     * @param hotelId - Hotel ID from URL path
     * @param capacity - Required room capacity
     * @param roomType - Room type filter
     * @param maxPrice - Maximum price per night
     * @param minPrice - Minimum price per night
     * @param checkInDate - Check-in date
     * @param checkOutDate - Check-out date
     * @return List of available rooms
     */
    @GetMapping("/rooms/{hotelId}")
    public ResponseEntity<List<RoomDto>> searchAvailableRoomsByParams(@PathVariable Long hotelId,
                                                                     @RequestParam(required = false) Integer capacity,
                                                                     @RequestParam(required = false) String roomType,
                                                                     @RequestParam(required = false) BigDecimal maxPrice,
                                                                     @RequestParam(required = false) BigDecimal minPrice,
                                                                     @RequestParam(required = false) String checkInDate,
                                                                     @RequestParam(required = false) String checkOutDate) {

        // Build SearchCriteriaDto from query parameters
        SearchCriteriaDto criteria = new SearchCriteriaDto();
        criteria.setCapacity(capacity);
        criteria.setRoomType(roomType);
        criteria.setMaxPrice(maxPrice);
        criteria.setMinPrice(minPrice);
        
        // Parse date strings to LocalDate objects
        if (checkInDate != null) {
            criteria.setCheckInDate(LocalDate.parse(checkInDate));
        }
        if (checkOutDate != null) {
            criteria.setCheckOutDate(LocalDate.parse(checkOutDate));
        }

        log.info("Searching available rooms for hotel: {} with parameters: {}", hotelId, criteria);
        List<RoomDto> rooms = searchService.searchAvailableRooms(hotelId, criteria);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Get hotels in specific price range
     * 
     * Use Case: Budget-based hotel search
     * 
     * Example: "Show me hotels with rooms under $100/night"
     * 
     * @param minPrice - Minimum price per night (optional)
     * @param maxPrice - Maximum price per night (optional)
     * @return Hotels with rooms in the specified price range
     */
    @GetMapping("/hotels/price-range")
    public ResponseEntity<List<HotelDto>> getHotelsByPriceRange(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        log.info("Getting hotels in price range: {} - {}", minPrice, maxPrice);
        List<HotelDto> hotels = searchService.getHotelsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(hotels);
    }

    /**
     * Get popular/highly-rated hotels
     * 
     * Use Case: Homepage recommendations, "Featured Hotels"
     * 
     * Algorithm:
     * 1. Filter hotels with rating >= 4.0
     * 2. Sort by rating (highest first)
     * 3. Limit to specified number
     * 
     * @param limit - Maximum number of hotels to return (default: 10)
     * @return List of popular hotels
     */
    @GetMapping("/hotels/popular")
    public ResponseEntity<List<HotelDto>> getPopularHotels(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting {} popular hotels", limit);
        List<HotelDto> hotels = searchService.getPopularHotels(limit);
        return ResponseEntity.ok(hotels);
    }

    /**
     * Test endpoint to verify search API is working
     * 
     * Use Case: Health check, API testing
     * 
     * @return Simple success message
     */
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Search API is working!");
    }
}
