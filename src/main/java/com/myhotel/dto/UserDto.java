package com.myhotel.dto;

import com.myhotel.entity.enums.Gender;
import com.myhotel.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * UserDto - Data Transfer Object for User entity
 * 
 * DTO Pattern Benefits:
 * - Decouples API from internal entity structure
 * - Allows selective field exposure
 * - Enables validation annotations
 * - Prevents exposing sensitive data
 * 
 * Security Note:
 * - Password field included for registration only
 * - Never return password in API responses
 * - Always hash passwords before storage
 */
@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: Generates no-args constructor (needed for JSON deserialization)
@AllArgsConstructor // Lombok: Generates constructor with all parameters
public class UserDto {

    private Long id;
    private String email;
    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Set<Role> roles;
    
    /**
     * Password field for registration
     * 
     * Security Considerations:
     * - Only used during user registration
     * - Never returned in API responses
     * - Always hashed before database storage
     * - Should be validated for strength
     */
    private String password;
}
