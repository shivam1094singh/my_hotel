package com.myhotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SearchCriteriaDto - Data Transfer Object for search parameters
 * 
 * DTOs are used to transfer data between layers (Controller -> Service -> Repository)
 * Benefits:
 * 1. Decouples API from internal entity structure
 * 2. Allows validation annotations
 * 3. Enables selective field updates
 * 4. Prevents exposing internal entity details
 * 
 * This DTO holds all possible search criteria for hotels and rooms
 * allowing flexible searching and filtering combinations
 */
@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: Generates no-args constructor (needed for JSON deserialization)
@AllArgsConstructor // Lombok: Generates constructor with all parameters
public class SearchCriteriaDto {

    /**
     * Location-based search criteria
     * Users can search by city or country
     */
    private String city;        // e.g., "New York", "London"
    private String country;      // e.g., "USA", "UK"

    /**
     * Hotel name keyword search
     * Allows partial matching on hotel names
     */
    private String nameKeyword; // e.g., "Hilton", "Marriott"

    /**
     * Rating-based filtering
     * Hotels are typically rated 1-5 stars
     */
    private BigDecimal minRating; // e.g., 4.0 (minimum 4 stars)
    private BigDecimal maxRating; // e.g., 5.0 (maximum 5 stars)

    /**
     * Price-based filtering for rooms
     * Price per night in local currency
     */
    private BigDecimal minPrice; // e.g., 50.00 (minimum $50/night)
    private BigDecimal maxPrice; // e.g., 200.00 (maximum $200/night)

    /**
     * Room capacity requirements
     * Number of guests the room should accommodate
     */
    private Integer capacity;    // e.g., 2 (for 2 guests)

    /**
     * Room type filtering
     * Different room categories available in hotels
     */
    private String roomType;    // e.g., "DELUXE", "SUITE", "STANDARD"

    /**
     * Date range for booking availability
     * Used to check if rooms are available for specific dates
     */
    private LocalDate checkInDate;  // Start date of stay
    private LocalDate checkOutDate; // End date of stay

    /**
     * Sorting options for search results
     * Allows users to order results by preference
     */
    private String sortBy = "name";   // Sort field: "name", "rating", "price"
    private String sortOrder = "asc"; // Sort direction: "asc" (ascending), "desc" (descending)
}
