package com.myhotel.service;

import com.myhotel.dto.UserDto;
import com.myhotel.dto.UserUpdateDto;
import com.myhotel.entity.User;

import java.util.List;

public interface UserService {

    User getUserById(Long id);

    UserDto convertToDto(User user);

    List<UserDto> getAllUsers();

    UserDto createUser(UserUpdateDto userDto);

    UserDto updateUser(Long id, UserUpdateDto updateDto);

    void deleteUser(Long id);
}
