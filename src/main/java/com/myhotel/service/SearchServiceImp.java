package com.myhotel.service;

import com.myhotel.dto.HotelDto;
import com.myhotel.dto.RoomDto;
import com.myhotel.dto.SearchCriteriaDto;
import com.myhotel.entity.Hotel;
import com.myhotel.entity.Room;
import com.myhotel.repository.HotelRepository;
import com.myhotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SearchServiceImp - Implementation of search and filtering business logic
 * 
 * Service Implementation Pattern:
 * - Contains the actual business logic
 * - Implements the interface contract
 * - Uses repositories to access data
 * - Uses ModelMapper to convert entities to DTOs
 * 
 * Key Dependencies:
 * - @RequiredArgsConstructor: Lombok creates constructor with all final fields
 * - @Service: Spring annotation for service layer component
 * - @Slf4j: Lombok for logging functionality
 * - ModelMapper: Maps entities to DTOs (prevents manual mapping)
 */
@Service // Marks this as a Spring service bean
@Slf4j // Lombok: Creates logger instance (log)
@RequiredArgsConstructor // Lombok: Creates constructor for dependency injection
public class SearchServiceImp implements SearchService {

    // Final fields - injected via constructor (dependency injection)
    private final HotelRepository hotelRepository; // Access to hotel data
    private final RoomRepository roomRepository;   // Access to room data
    private final ModelMapper modelMapper;         // Entity-to-DTO converter
    private final BookingService bookingService;   // Check room availability

    /**
     * Search hotels with complex filtering
     * 
     * Algorithm Flow:
     * 1. Get base hotel list based on location (city/country)
     * 2. Convert entities to DTOs (using ModelMapper)
     * 3. Apply filters (rating, name keyword)
     * 4. If dates provided, check room availability
     * 5. Return filtered list
     * 
     * @param criteria - Search parameters from client request
     * @return Filtered list of hotels as DTOs
     */
    @Override
    public List<HotelDto> searchHotels(SearchCriteriaDto criteria) {
        // Step 1: Get base hotel list
        List<Hotel> hotels;
        
        if (criteria.getCity() != null && !criteria.getCity().isEmpty()) {
            // Search by city
            hotels = hotelRepository.findByCity(criteria.getCity());
        } else if (criteria.getCountry() != null && !criteria.getCountry().isEmpty()) {
            // Search by country
            hotels = hotelRepository.findByCountry(criteria.getCountry());
        } else {
            // Get all hotels if no location specified
            hotels = hotelRepository.findAll();
        }

        // Step 2: Convert entities to DTOs using ModelMapper
        // ModelMapper automatically maps fields with same names
        List<HotelDto> filteredHotels = hotels.stream()
                .map(hotel -> modelMapper.map(hotel, HotelDto.class)) // Entity -> DTO
                .filter(hotelDto -> applyFilters(hotelDto, criteria)) // Apply filters
                .collect(Collectors.toList());

        // Step 3: If room search criteria provided, check availability
        if (criteria.getCheckInDate() != null && criteria.getCheckOutDate() != null 
            && criteria.getCapacity() != null) {
            // Only keep hotels that have available rooms for the dates
            filteredHotels = filteredHotels.stream()
                    .filter(hotelDto -> hasAvailableRooms(hotelDto, criteria))
                    .collect(Collectors.toList());
        }

        log.info("Found {} hotels matching criteria", filteredHotels.size());
        return filteredHotels;
    }

    /**
     * Search available rooms for a specific hotel
     * 
     * Process:
     * 1. Get rooms for the hotel (filtered by type if specified)
     * 2. Filter by availability (room.available = true)
     * 3. Convert to DTOs
     * 4. Apply additional filters (capacity, price)
     * 5. Check booking conflicts if dates provided
     * 
     * @param hotelId - Hotel to search rooms for
     * @param criteria - Room search parameters
     * @return List of available rooms as DTOs
     */
    @Override
    public List<RoomDto> searchAvailableRooms(Long hotelId, SearchCriteriaDto criteria) {
        // Step 1: Get rooms for the hotel
        List<Room> rooms;
        
        if (criteria.getRoomType() != null && !criteria.getRoomType().isEmpty()) {
            // Filter by room type (e.g., "DELUXE", "SUITE")
            rooms = roomRepository.findByHotelIdAndRoomType(hotelId, criteria.getRoomType());
        } else {
            // Get all rooms for the hotel
            rooms = roomRepository.findByHotelId(hotelId);
        }

        // Step 2: Filter available rooms and convert to DTOs
        List<RoomDto> availableRooms = rooms.stream()
                .filter(Room::getAvailable) // Only available rooms
                .map(room -> modelMapper.map(room, RoomDto.class)) // Entity -> DTO
                .filter(roomDto -> applyRoomFilters(roomDto, criteria)) // Apply filters
                .collect(Collectors.toList());

        // Step 3: Check booking availability if dates provided
        if (criteria.getCheckInDate() != null && criteria.getCheckOutDate() != null) {
            // Filter out rooms that are booked for the requested dates
            availableRooms = availableRooms.stream()
                    .filter(roomDto -> bookingService.isRoomAvailable(
                            roomDto.getId(), criteria.getCheckInDate(), criteria.getCheckOutDate()))
                    .collect(Collectors.toList());
        }

        log.info("Found {} available rooms for hotel: {}", availableRooms.size(), hotelId);
        return availableRooms;
    }

    /**
     * Get hotels with rooms in specific price range
     * 
     * Logic:
     * 1. Get all hotels
     * 2. Convert to DTOs
     * 3. Check if hotel has any rooms in the price range
     * 4. Return matching hotels
     * 
     * @param minPrice - Minimum price (null = no minimum)
     * @param maxPrice - Maximum price (null = no maximum)
     * @return Hotels with rooms in price range
     */
    @Override
    public List<HotelDto> getHotelsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Hotel> hotels = hotelRepository.findAll();
        
        return hotels.stream()
                .map(hotel -> modelMapper.map(hotel, HotelDto.class))
                .filter(hotel -> hasRoomsInPriceRange(hotel.getId(), minPrice, maxPrice))
                .collect(Collectors.toList());
    }

    /**
     * Get popular hotels (highly rated)
     * 
     * Algorithm:
     * 1. Get all hotels
     * 2. Convert to DTOs
     * 3. Filter by rating (>= 4.0)
     * 4. Sort by rating (descending)
     * 5. Limit results
     * 
     * @param limit - Maximum number of results
     * @return List of popular hotels
     */
    @Override
    public List<HotelDto> getPopularHotels(int limit) {
        List<Hotel> hotels = hotelRepository.findAll();
        
        return hotels.stream()
                .map(hotel -> modelMapper.map(hotel, HotelDto.class))
                .filter(hotel -> hotel.getRating() != null && hotel.getRating().compareTo(BigDecimal.valueOf(4.0)) >= 0)
                .sorted((h1, h2) -> h2.getRating().compareTo(h1.getRating())) // Sort by rating (high to low)
                .limit(limit) // Limit to specified number
                .collect(Collectors.toList());
    }

    /**
     * Helper method to apply hotel filters
     * 
     * Filters applied:
     * - Minimum rating
     * - Maximum rating
     * - Name keyword (case-insensitive contains)
     * 
     * @param hotel - Hotel DTO to check
     * @param criteria - Search criteria
     * @return true if hotel passes all filters
     */
    private boolean applyFilters(HotelDto hotel, SearchCriteriaDto criteria) {
        // Rating filters
        if (criteria.getMinRating() != null && 
            (hotel.getRating() == null || hotel.getRating().compareTo(criteria.getMinRating()) < 0)) {
            return false; // Hotel rating below minimum
        }

        if (criteria.getMaxRating() != null && 
            (hotel.getRating() == null || hotel.getRating().compareTo(criteria.getMaxRating()) > 0)) {
            return false; // Hotel rating above maximum
        }

        // Name keyword filter (case-insensitive)
        if (criteria.getNameKeyword() != null && !criteria.getNameKeyword().isEmpty() &&
            (hotel.getName() == null || !hotel.getName().toLowerCase()
                    .contains(criteria.getNameKeyword().toLowerCase()))) {
            return false; // Hotel name doesn't contain keyword
        }

        return true; // Hotel passes all filters
    }

    /**
     * Helper method to apply room filters
     * 
     * Filters applied:
     * - Capacity (must meet or exceed required)
     * - Price range
     * 
     * @param room - Room DTO to check
     * @param criteria - Search criteria
     * @return true if room passes all filters
     */
    private boolean applyRoomFilters(RoomDto room, SearchCriteriaDto criteria) {
        // Capacity filter
        if (criteria.getCapacity() != null && room.getCapacity() < criteria.getCapacity()) {
            return false; // Room too small
        }

        // Price filters
        if (criteria.getMaxPrice() != null && room.getPricePerNight().compareTo(criteria.getMaxPrice()) > 0) {
            return false; // Room too expensive
        }

        if (criteria.getMinPrice() != null && room.getPricePerNight().compareTo(criteria.getMinPrice()) < 0) {
            return false; // Room too cheap
        }

        return true; // Room passes all filters
    }

    /**
     * Check if hotel has available rooms for the criteria
     * 
     * @param hotel - Hotel to check
     * @param criteria - Room requirements
     * @return true if hotel has suitable available rooms
     */
    private boolean hasAvailableRooms(HotelDto hotel, SearchCriteriaDto criteria) {
        List<RoomDto> availableRooms = searchAvailableRooms(hotel.getId(), criteria);
        return !availableRooms.isEmpty();
    }

    /**
     * Check if hotel has rooms in specified price range
     * 
     * @param hotelId - Hotel to check
     * @param minPrice - Minimum price
     * @param maxPrice - Maximum price
     * @return true if hotel has rooms in price range
     */
    private boolean hasRoomsInPriceRange(Long hotelId, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Room> rooms = roomRepository.findByHotelId(hotelId);
        
        return rooms.stream()
                .filter(Room::getAvailable) // Only available rooms
                .anyMatch(room -> {
                    BigDecimal price = room.getPricePerNight();
                    return (minPrice == null || price.compareTo(minPrice) >= 0) &&
                           (maxPrice == null || price.compareTo(maxPrice) <= 0);
                });
    }
}
