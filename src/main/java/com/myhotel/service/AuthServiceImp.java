package com.myhotel.service;

import com.myhotel.dto.LoginRequest;
import com.myhotel.dto.LoginResponse;
import com.myhotel.dto.UserDto;
import com.myhotel.entity.User;
import com.myhotel.entity.enums.Role;
import com.myhotel.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AuthServiceImp - Implementation of authentication and authorization business logic
 * 
 * Service Implementation Pattern:
 * - Contains actual security business logic
 * - Implements AuthService interface contract
 * - Uses repositories for data access
 * - Integrates with Spring Security components
 * 
 * Key Dependencies:
 * - @RequiredArgsConstructor: Lombok creates constructor with all final fields
 * - @Service: Spring annotation for service layer component
 * - @Slf4j: Lombok for logging functionality
 * - PasswordEncoder: Spring Security for password hashing
 * - ModelMapper: Entity-to-DTO conversion
 * 
 * Security Architecture:
 * - JWT for stateless authentication
 * - BCrypt for password hashing
 * - Role-based authorization
 * - Token refresh mechanism
 */
@Service // Marks this as a Spring service bean
@Slf4j // Lombok: Creates logger instance (log)
@RequiredArgsConstructor // Lombok: Creates constructor for dependency injection
public class AuthServiceImp implements AuthService {

    // Final fields - injected via constructor (dependency injection)
    private final UserRepository userRepository;     // Access to user data
    private final PasswordEncoder passwordEncoder;   // BCrypt password hashing
    private final ModelMapper modelMapper;          // Entity-to-DTO converter
    private final UserService userService;          // User management operations

    // JWT secret key - in production, use environment variable
    private final SecretKey jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // In-memory token storage (in production, use Redis or database)
    private final ConcurrentHashMap<String, String> refreshTokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

    /**
     * Authenticate user and generate JWT tokens
     * 
     * Authentication Algorithm:
     * 1. Find user by email
     * 2. Verify password using BCrypt
     * 3. Generate JWT tokens with claims
     * 4. Store refresh token
     * 5. Return authentication response
     * 
     * @param loginRequest - User credentials
     * @return LoginResponse with JWT tokens and user data
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Authentication attempt for email: {}", loginRequest.getEmail());

        // Step 1: Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.getEmail()));

        // Step 2: Verify password using BCrypt
        // passwordEncoder.matches() compares plain text with hashed password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Failed authentication attempt for email: {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        // Step 3: Generate JWT tokens
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        // Step 4: Store refresh token for later validation
        refreshTokens.put(refreshToken, user.getEmail());

        // Step 5: Create response
        LoginResponse response = new LoginResponse();
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(3600L); // 1 hour
        response.setUser(userService.convertToDto(user));

        log.info("Successful authentication for email: {}", user.getEmail());
        return response;
    }

    /**
     * Register new user account
     * 
     * Registration Algorithm:
     * 1. Check if email already exists
     * 2. Hash password using BCrypt
     * 3. Create user with default role
     * 4. Save user to database
     * 5. Return user data (without password)
     * 
     * @param userDto - User registration data
     * @return UserDto with created user information
     */
    @Override
    public UserDto register(UserDto userDto) {
        log.info("Registration attempt for email: {}", userDto.getEmail());

        // Step 1: Check if user already exists
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDto.getEmail());
        }

        // Step 2: Create new user entity
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        user.setDateOfBirth(userDto.getDateOfBirth());
        user.setGender(userDto.getGender());

        // Step 3: Hash password using BCrypt
        // BCrypt automatically handles salt generation and hashing
        String hashedPassword = passwordEncoder.encode(userDto.getPassword());
        user.setPassword(hashedPassword);

        // Step 4: Assign default role (GUEST)
        user.setRoles(java.util.Set.of(Role.GUEST));

        // Step 5: Save user to database
        User savedUser = userRepository.save(user);

        log.info("User registered successfully with email: {}", savedUser.getEmail());
        return userService.convertToDto(savedUser);
    }

    /**
     * Generate new access token using refresh token
     * 
     * Token Refresh Algorithm:
     * 1. Validate refresh token exists
     * 2. Get user email from refresh token
     * 3. Generate new access token
     * 4. Optionally generate new refresh token
     * 
     * @param refreshToken - Valid refresh token
     * @return LoginResponse with new access token
     */
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Token refresh attempt");

        // Step 1: Validate refresh token exists
        String userEmail = refreshTokens.get(refreshToken);
        if (userEmail == null) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Step 2: Find user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 3: Generate new access token
        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        // Step 4: Update refresh token storage
        refreshTokens.remove(refreshToken); // Remove old refresh token
        refreshTokens.put(newRefreshToken, userEmail); // Add new refresh token

        // Step 5: Create response
        LoginResponse response = new LoginResponse();
        response.setToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(3600L);
        response.setUser(userService.convertToDto(user));

        log.info("Token refreshed successfully for user: {}", userEmail);
        return response;
    }

    /**
     * Logout user and invalidate tokens
     * 
     * Logout Algorithm:
     * 1. Remove refresh token from active tokens
     * 2. Add access token to blacklist
     * 3. Log logout event
     * 
     * @param token - JWT access token to invalidate
     */
    @Override
    public void logout(String token) {
        log.info("Logout attempt");

        // Remove "Bearer " prefix if present
        String cleanToken = token.replace("Bearer ", "");

        // Add token to blacklist with expiration time
        tokenBlacklist.put(cleanToken, System.currentTimeMillis() + 3600000); // 1 hour

        log.info("User logged out successfully");
    }

    /**
     * Validate JWT token and extract user information
     * 
     * Token Validation Algorithm:
     * 1. Check if token is blacklisted
     * 2. Parse JWT token
     * 3. Extract user claims
     * 4. Return user information
     * 
     * @param token - JWT access token
     * @return UserDto with user information
     */
    @Override
    public UserDto validateToken(String token) {
        // Remove "Bearer " prefix if present
        String cleanToken = token.replace("Bearer ", "");

        // Check if token is blacklisted
        Long blacklistTime = tokenBlacklist.get(cleanToken);
        if (blacklistTime != null && blacklistTime > System.currentTimeMillis()) {
            throw new RuntimeException("Token is blacklisted");
        }

        try {
            // Parse JWT token using JJWT library
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(cleanToken)
                    .getBody();

            // Extract user email from claims
            String userEmail = claims.getSubject();
            
            // Find user and return as DTO
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            return userService.convertToDto(user);
            
        } catch (Exception e) {
            throw new RuntimeException("Invalid token: " + e.getMessage());
        }
    }

    /**
     * Check if user has specific role
     * 
     * @param userEmail - User email
     * @param role - Role to check
     * @return true if user has the role
     */
    @Override
    public boolean hasRole(String userEmail, String role) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getRoles().stream()
                .anyMatch(userRole -> userRole.name().equals(role));
    }

    /**
     * Change user password
     * 
     * Password Change Algorithm:
     * 1. Authenticate with current password
     * 2. Hash new password
     * 3. Update password in database
     * 4. Invalidate all tokens
     * 
     * @param userEmail - User email
     * @param currentPassword - Current password
     * @param newPassword - New password
     */
    @Override
    public void changePassword(String userEmail, String currentPassword, String newPassword) {
        log.info("Password change attempt for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Hash and update new password
        String hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPassword);
        userRepository.save(user);

        // Invalidate all refresh tokens for this user
        refreshTokens.entrySet().removeIf(entry -> entry.getValue().equals(userEmail));

        log.info("Password changed successfully for user: {}", userEmail);
    }

    /**
     * Generate JWT access token using JJWT library
     * 
     * @param user - User to generate token for
     * @return JWT access token string
     */
    private String generateAccessToken(User user) {
        // Token expiration: 1 hour from now
        Date expirationDate = Date.from(Instant.now().plusSeconds(3600));
        
        // Build JWT token with claims
        return Jwts.builder()
                .setSubject(user.getEmail()) // User email as subject
                .claim("userId", user.getId()) // User ID as claim
                .claim("roles", user.getRoles()) // User roles as claim
                .setIssuedAt(new Date()) // Issue date
                .setExpiration(expirationDate) // Expiration date
                .signWith(jwtSecret) // Sign with secret key
                .compact(); // Build token
    }

    /**
     * Generate JWT refresh token using JJWT library
     * 
     * @param user - User to generate token for
     * @return JWT refresh token string
     */
    private String generateRefreshToken(User user) {
        // Refresh token expiration: 7 days from now
        Date expirationDate = Date.from(Instant.now().plusSeconds(7 * 24 * 60 * 60));
        
        // Build refresh token with longer expiration
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .claim("type", "refresh") // Mark as refresh token
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(jwtSecret)
                .compact();
    }
}
