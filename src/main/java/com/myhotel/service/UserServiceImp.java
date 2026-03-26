package com.myhotel.service;

import com.myhotel.dto.UserDto;
import com.myhotel.dto.UserUpdateDto;
import com.myhotel.entity.User;
import com.myhotel.entity.enums.Role;
import com.myhotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

        @Override
    public UserDto convertToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(UserUpdateDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDto.getEmail());
        }

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword() != null ? userDto.getPassword() : "default123");
        user.setName(userDto.getName());
        user.setDateOfBirth(userDto.getDateOfBirth());
        user.setGender(userDto.getGender());
        user.setRoles(userDto.getRoles() != null ? userDto.getRoles() : Set.of(Role.GUEST));

        User savedUser = userRepository.save(user);
        log.info("User created successfully with email: {}", savedUser.getEmail());
        return convertToDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long id, UserUpdateDto updateDto) {
        User user = getUserById(id);
        
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new RuntimeException("Email already exists: " + updateDto.getEmail());
            }
            user.setEmail(updateDto.getEmail());
        }
        
        if (updateDto.getName() != null) {
            user.setName(updateDto.getName());
        }
        
        if (updateDto.getDateOfBirth() != null) {
            user.setDateOfBirth(updateDto.getDateOfBirth());
        }
        
        if (updateDto.getGender() != null) {
            user.setGender(updateDto.getGender());
        }
        
        if (updateDto.getRoles() != null) {
            user.setRoles(updateDto.getRoles());
        }

        if (updateDto.getPassword() != null) {
            user.setPassword(updateDto.getPassword());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated with email: {}", updatedUser.getEmail());
        return convertToDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted with id: {}", id);
    }
}
