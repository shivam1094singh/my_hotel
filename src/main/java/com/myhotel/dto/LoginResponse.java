package com.myhotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginResponse - Data Transfer Object for authentication response
 * 
 * JWT Authentication Flow:
 * 1. User credentials validated against database
 * 2. JWT token generated with user claims
 * 3. Refresh token generated for token renewal
 * 4. Client stores tokens for subsequent API calls
 * 
 * Token Types:
 * - Access Token: Short-lived (15-30 minutes), used for API calls
 * - Refresh Token: Long-lived (days/weeks), used to get new access tokens
 * 
 * Security Best Practices:
 * - Use HTTPS for all authentication endpoints
 * - Set appropriate expiration times
 * - Implement token revocation on logout
 * - Store tokens securely on client side
 */
@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: No-args constructor for JSON serialization
@AllArgsConstructor // Lombok: All-args constructor for easy creation
public class LoginResponse {

    /**
     * JWT Access Token
     * 
     * Purpose:
     * - Authenticate API requests
     * - Contains user claims (id, roles, permissions)
     * - Short-lived for security (15-30 minutes)
     * 
     * Format: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * 
     * Usage: Added to Authorization header as "Bearer {token}"
     */
    private String token;

    /**
     * JWT Refresh Token
     * 
     * Purpose:
     * - Obtain new access tokens without re-authentication
     * - Long-lived (7-30 days)
     * - Stored securely on client (httpOnly cookie recommended)
     * 
     * Security:
     * - Can be revoked by server
     * - Single-use or reusable based on implementation
     */
    private String refreshToken;

    /**
     * Token type specification
     * 
     * Purpose:
     * - Indicates token format for client
     * - Standard OAuth 2.0 specification
     * - Always "Bearer" for JWT tokens
     * 
     * Usage: Authorization: Bearer {token}
     */
    private String type = "Bearer";

    /**
     * Token expiration time in seconds
     * 
     * Purpose:
     * - Informs client when token expires
     * - Enables proactive token refresh
     * - Helps with session management
     * 
     * Example: 1800 (30 minutes), 3600 (1 hour)
     */
    private Long expiresIn;

    /**
     * User information for client display
     * 
     * Purpose:
     * - Client can display user info without separate API call
     * - Reduces additional requests after login
     * - Includes only safe user data (no password/hash)
     * 
     * Security Note: Only include non-sensitive user information
     */
    private UserDto user;
}
