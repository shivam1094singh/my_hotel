package com.myhotel.service;

import com.myhotel.dto.LoginRequest;
import com.myhotel.dto.LoginResponse;
import com.myhotel.dto.UserDto;

/**
 * AuthService - Interface for authentication and authorization operations
 * 
 * Authentication Service Pattern:
 * - Separates authentication logic from business logic
 * - Provides clean API for security operations
 * - Enables different authentication implementations
 * - Supports testing with mock implementations
 * 
 * Security Flow:
 * 1. Client provides credentials
 * 2. Service validates credentials
 * 3. Service generates JWT tokens
 * 4. Client receives tokens for API access
 * 5. Subsequent requests use tokens for authorization
 * 
 * Key Concepts:
 * - JWT (JSON Web Tokens): Stateless authentication
 * - Access Token: Short-lived API access token
 * - Refresh Token: Long-lived token renewal
 * - Role-based Authorization: User permissions
 */
public interface AuthService {

    /**
     * Authenticate user and generate tokens
     * 
     * Authentication Flow:
     * 1. Validate email format and existence
     * 2. Verify password against hashed database value
     * 3. Generate JWT access token with user claims
     * 4. Generate refresh token for token renewal
     * 5. Return tokens and user information
     * 
     * Security Implementation:
     * - Use BCrypt for password hashing
     * - Include user roles in JWT claims
     * - Set appropriate token expiration times
     * - Log authentication attempts (success/failure)
     * 
     * @param loginRequest - User credentials (email, password)
     * @return LoginResponse with tokens and user data
     * @throws RuntimeException if authentication fails
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * Register new user account
     * 
     * Registration Flow:
     * 1. Validate user data (email format, password strength)
     * 2. Check if email already exists
     * 3. Hash password using BCrypt
     * 4. Save user with default role (GUEST)
     * 5. Return user data (without sensitive information)
     * 
     * Security Considerations:
     * - Always hash passwords before storage
     * - Assign default least-privileged role
     * - Validate email uniqueness
     * - Send welcome email (optional)
     * 
     * @param userDto - User registration data
     * @return UserDto with created user information
     * @throws RuntimeException if registration fails
     */
    UserDto register(UserDto userDto);

    /**
     * Generate new access token using refresh token
     * 
     * Token Refresh Flow:
     * 1. Validate refresh token signature and expiration
     * 2. Extract user information from token
     * 3. Generate new access token with updated claims
     * 4. Optionally generate new refresh token
     * 5. Return new tokens to client
     * 
     * Security Benefits:
     * - Users stay logged in without re-authentication
     * - Access tokens remain short-lived for security
     * - Refresh tokens can be revoked if compromised
     * - Reduces password exposure frequency
     * 
     * @param refreshToken - Valid refresh token
     * @return LoginResponse with new access token
     * @throws RuntimeException if refresh token is invalid
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * Logout user and invalidate tokens
     * 
     * Logout Flow:
     * 1. Extract token from Authorization header
     * 2. Add token to blacklist/revocation list
     * 3. Remove refresh token from active tokens
     * 4. Log logout event for audit trail
     * 
     * Security Implementation:
     * - Maintain token blacklist in Redis/database
     * - Set short expiration for blacklisted tokens
     * - Clear client-side tokens (cookies/storage)
     * - Implement token revocation endpoint
     * 
     * @param token - JWT access token to invalidate
     */
    void logout(String token);

    /**
     * Validate JWT token and extract user information
     * 
     * Token Validation Flow:
     * 1. Check token signature using secret key
     * 2. Verify token expiration time
     * 3. Check if token is blacklisted/revoked
     * 4. Extract user claims (id, email, roles)
     * 5. Return user information for authorization
     * 
     * Usage:
     * - Called by JWT filter on each protected request
     * - Sets SecurityContext with user details
     * - Enables method-level security annotations
     * - Supports role-based access control
     * 
     * @param token - JWT access token
     * @return User information extracted from token
     * @throws RuntimeException if token is invalid
     */
    UserDto validateToken(String token);

    /**
     * Check if user has specific role/permission
     * 
     * Authorization Flow:
     * 1. Get user from current security context
     * 2. Check if user has required role
     * 3. Return boolean for authorization decision
     * 
     * Role Hierarchy (Example):
     * - GUEST: Can view hotels, make bookings
     * - HOTEL_MANAGER: Can manage own hotels
     * - ADMIN: Can manage all system data
     * 
     * @param userEmail - User email to check
     * @param role - Role to verify
     * @return true if user has the specified role
     */
    boolean hasRole(String userEmail, String role);

    /**
     * Change user password
     * 
     * Password Change Flow:
     * 1. Authenticate user with current password
     * 2. Validate new password strength
     * 3. Hash new password using BCrypt
     * 4. Update password in database
     * 5. Invalidate all existing tokens (force re-login)
     * 
     * Security Best Practices:
     * - Always require current password verification
     * - Enforce password complexity requirements
     * - Force token invalidation after password change
     * - Send password change notification email
     * 
     * @param userEmail - User email
     * @param currentPassword - Current password for verification
     * @param newPassword - New password to set
     * @throws RuntimeException if password change fails
     */
    void changePassword(String userEmail, String currentPassword, String newPassword);
}
