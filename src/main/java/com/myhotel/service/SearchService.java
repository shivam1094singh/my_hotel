package com.myhotel.service;

import com.myhotel.dto.HotelDto;
import com.myhotel.dto.RoomDto;
import com.myhotel.dto.SearchCriteriaDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * SearchService - Interface for search and filtering operations
 * 
 * Service Layer Pattern:
 * - Defines business operations that can be performed
 * - Hides implementation details from the controller layer
 * - Provides contract for what search functionality is available
 * - Enables dependency injection and testing (mocking)
 * 
 * Flow: Controller -> Service Interface -> Service Implementation -> Repository
 */
public interface SearchService {

    /**
     * Search hotels based on multiple criteria
     * 
     * @param criteria - SearchCriteriaDto containing all search parameters
     * @return List of HotelDto matching the criteria
     * 
     * Usage Flow:
     * 1. Controller receives search request from client (HTTP POST/GET)
     * 2. Controller calls this method with SearchCriteriaDto
     * 3. Implementation filters hotels based on criteria
     * 4. Returns list of hotels as DTOs (not entities)
     * 5. Controller returns JSON response to client
     */
    List<HotelDto> searchHotels(SearchCriteriaDto criteria);

    /**
     * Search available rooms for a specific hotel
     * 
     * @param hotelId - ID of the hotel to search rooms for
     * @param criteria - Search criteria including dates, capacity, price range
     * @return List of available RoomDto objects
     * 
     * Key Features:
     * - Checks room availability for specific dates
     * - Filters by capacity and price
     * - Only returns rooms that are actually bookable
     */
    List<RoomDto> searchAvailableRooms(Long hotelId, SearchCriteriaDto criteria);

    /**
     * Find hotels with rooms in specific price range
     * 
     * @param minPrice - Minimum price per night (can be null for no minimum)
     * @param maxPrice - Maximum price per night (can be null for no maximum)
     * @return List of HotelDto with rooms in the price range
     * 
     * Use Case:
     * - Budget-conscious travelers
     * - "Show me hotels under $100/night"
     * - Price comparison between hotels
     */
    List<HotelDto> getHotelsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Get popular/highly-rated hotels
     * 
     * @param limit - Maximum number of hotels to return
     * @return List of popular HotelDto objects
     * 
     * Algorithm:
     * 1. Filter hotels with rating >= 4.0
     * 2. Sort by rating (highest first)
     * 3. Limit to specified number
     * 
     * Business Logic:
     * - "Popular" defined as 4+ stars
     * - Helps users discover quality hotels
     * - Used for homepage recommendations
     */
    List<HotelDto> getPopularHotels(int limit);
}
