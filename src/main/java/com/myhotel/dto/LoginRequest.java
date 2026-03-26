package com.myhotel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginRequest - Data Transfer Object for user login
 * 
 * Authentication Flow:
 * 1. Client sends login credentials (email + password)
 * 2. Spring validates using @NotBlank and @Email annotations
 * 3. Controller receives validated LoginRequest object
 * 4. Service authenticates user and generates JWT token
 * 5. Client receives token for subsequent requests
 * 
 * Security Benefits:
 * - DTO prevents exposing internal User entity structure
 * - Validation ensures data integrity before processing
 * - Separates authentication from user management
 */
@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: No-args constructor for JSON deserialization
@AllArgsConstructor // Lombok: All-args constructor for testing
public class LoginRequest {

    /**
     * User's email address - used as username
     * 
     * Validation:
     * - @NotBlank: Cannot be null or empty
     * - @Email: Must be valid email format
     * 
     * Security Note: Email is used as unique identifier
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * User's password
     * 
     * Validation:
     * - @NotBlank: Cannot be null or empty
     * 
     * Security Note: Password is validated against hashed version in database
     * Never store plain text passwords - always use bcrypt/argon2
     */
    @NotBlank(message = "Password is required")
    private String password;
}
