package com.myhotel.controller;

import com.myhotel.dto.LoginRequest;
import com.myhotel.dto.LoginResponse;
import com.myhotel.dto.UserDto;
import com.myhotel.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST API endpoints for authentication and authorization
 * 
 * Authentication Controller Pattern:
 * - Handles user authentication (login, register, logout)
 * - Manages JWT token operations
 * - Provides user account management
 * - Secures endpoints with proper validation
 * 
 * Security Architecture:
 * - JWT (JSON Web Tokens) for stateless authentication
 * - Role-based authorization
 * - Token refresh mechanism
 * - Password hashing with BCrypt
 * 
 * Key Annotations:
 * - @RestController: Combines @Controller and @ResponseBody
 * - @RequestMapping: Base URL for all authentication endpoints
 * - @RequiredArgsConstructor: Lombok for dependency injection
 * - @Slf4j: Lombok for logging
 * - @Valid: Enables validation of request bodies
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    // Dependency injection - Spring injects AuthService implementation
    private final AuthService authService;

    /**
     * Register new user account
     * 
     * Registration Flow:
     * 1. Client sends POST request with user data
     * 2. Spring validates @Valid UserDto (email format, required fields)
     * 3. Controller calls authService.register()
     * 4. Service validates email uniqueness, hashes password, saves user
     * 5. Returns created user data (without password)
     * 
     * Security Considerations:
     * - Always hash passwords before storage
     * - Assign least-privileged default role
     * - Validate email format and uniqueness
     * - Return only safe user data (no passwords/hashes)
     * 
     * @param userDto - User registration data with validation
     * @return ResponseEntity with created UserDto and HTTP 201 status
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserDto userDto) {
        log.info("Registration attempt for email: {}", userDto.getEmail());
        
        // Delegate registration logic to service layer
        UserDto registeredUser = authService.register(userDto);
        
        // Return 201 Created status for successful registration
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    /**
     * Authenticate user and generate JWT tokens
     * 
     * Login Flow:
     * 1. Client sends POST request with credentials
     * 2. Spring validates @Valid LoginRequest (email format, required fields)
     * 3. Controller calls authService.login()
     * 4. Service verifies credentials, generates JWT tokens
     * 5. Returns tokens and user data for client storage
     * 
     * JWT Token Structure:
     * - Access Token: Short-lived (15-30 minutes), used for API calls
     * - Refresh Token: Long-lived (7-30 days), used for token renewal
     * - User Data: User information for immediate display
     * 
     * Client Usage:
     * - Store access token in memory/session storage
     * - Store refresh token securely (httpOnly cookie recommended)
     * - Use "Authorization: Bearer {token}" header for API calls
     * 
     * @param loginRequest - User credentials with validation
     * @return ResponseEntity with LoginResponse containing JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        
        // Authenticate user and generate tokens
        LoginResponse response = authService.login(loginRequest);
        
        // Return 200 OK with authentication tokens
        return ResponseEntity.ok(response);
    }

    /**
     * Generate new access token using refresh token
     * 
     * Token Refresh Flow:
     * 1. Client sends POST request with refresh token in header
     * 2. Controller extracts refresh token from Authorization header
     * 3. Service validates refresh token and generates new access token
     * 4. Client receives new tokens without re-authentication
     * 
     * Security Benefits:
     * - Users stay logged in without password re-entry
     * - Access tokens remain short-lived for security
     * - Refresh tokens can be revoked if compromised
     * - Reduces password exposure frequency
     * 
     * Client Implementation:
     * - Check access token expiration before API calls
     * - Use refresh token to get new access token
     * - Update stored tokens with new values
     * - Handle refresh token expiration gracefully
     * 
     * @param refreshToken - Valid refresh token from Authorization header
     * @return ResponseEntity with new LoginResponse containing fresh tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        log.info("Token refresh attempt");
        
        // Generate new access token using refresh token
        LoginResponse response = authService.refreshToken(refreshToken);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user and invalidate tokens
     * 
     * Logout Flow:
     * 1. Client sends POST request with access token
     * 2. Controller extracts token from Authorization header
     * 3. Service adds token to blacklist and removes refresh token
     * 4. Client clears stored tokens
     * 
     * Security Implementation:
     * - Maintain token blacklist in Redis/database
     * - Set expiration time for blacklisted tokens
     * - Remove refresh token from active tokens
     * - Log logout events for audit trail
     * 
     * Client Cleanup:
     * - Clear access token from memory/session
     * - Clear refresh token from secure storage
     * - Redirect to login page
     * - Clear any user-specific cached data
     * 
     * @param token - JWT access token from Authorization header
     * @return ResponseEntity with success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout attempt");
        
        // Invalidate tokens and logout user
        authService.logout(token);
        
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Validate token and get current user information
     * 
     * Token Validation Flow:
     * 1. Client sends GET request with access token
     * 2. Controller extracts token from Authorization header
     * 3. Service validates token and extracts user claims
     * 4. Returns current user information
     * 
     * Use Cases:
     * - Client startup validation of stored tokens
     * - User profile display after login
     * - Token expiration checking
     * - Session validation
     * 
     * Error Handling:
     * - 401 Unauthorized if token is invalid/expired
     * - 403 Forbidden if token is blacklisted
     * - Proper error messages for client handling
     * 
     * @param token - JWT access token from Authorization header
     * @return ResponseEntity with current UserDto
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader("Authorization") String token) {
        log.info("Current user request");
        
        // Validate token and get user information
        UserDto currentUser = authService.validateToken(token);
        
        return ResponseEntity.ok(currentUser);
    }

    /**
     * Change user password
     * 
     * Password Change Flow:
     * 1. Client sends POST request with current and new passwords
     * 2. Service authenticates with current password
     * 3. Service hashes new password and updates database
     * 4. Service invalidates all existing tokens (forces re-login)
     * 
     * Security Best Practices:
     * - Always require current password verification
     * - Enforce password complexity requirements
     * - Force token invalidation after password change
     * - Send password change notification email
     * - Log password change events for audit
     * 
     * Client Implementation:
     * - Require current password for security
     * - Validate new password strength
     * - Clear stored tokens after successful change
     * - Force user to re-login with new password
     * - Show success/error messages to user
     * 
     * @param userEmail - User email identifying the account
     * @param currentPassword - Current password for verification
     * @param newPassword - New password to set
     * @return ResponseEntity with success message
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestParam String userEmail,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        
        log.info("Password change attempt for user: {}", userEmail);
        
        // Change password with security validation
        authService.changePassword(userEmail, currentPassword, newPassword);
        
        return ResponseEntity.ok("Password changed successfully. Please login again.");
    }

    /**
     * Check if user has specific role/permission
     * 
     * Role Check Flow:
     * 1. Client sends GET request with user email and role
     * 2. Service checks user's assigned roles
     * 3. Returns boolean indicating role membership
     * 
     * Role Hierarchy Example:
     * - GUEST: Can view hotels, make bookings
     * - HOTEL_MANAGER: Can manage own hotels, rooms, bookings
     * - ADMIN: Can manage all system data, users, settings
     * 
     * Use Cases:
     * - Conditional UI rendering based on user roles
     * - Feature access validation
     * - Permission checking before API calls
     * - Dynamic menu generation
     * 
     * @param userEmail - User email to check
     * @param role - Role to verify (GUEST, HOTEL_MANAGER, ADMIN)
     * @return ResponseEntity with boolean indicating role membership
     */
    @GetMapping("/has-role")
    public ResponseEntity<Boolean> hasRole(
            @RequestParam String userEmail,
            @RequestParam String role) {
        
        boolean hasRole = authService.hasRole(userEmail, role);
        
        return ResponseEntity.ok(hasRole);
    }

    /**
     * Test endpoint to verify authentication API is working
     * 
     * Use Case: Health check, API testing, connectivity verification
     * 
     * @return Simple success message
     */
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Authentication API is working!");
    }
}
