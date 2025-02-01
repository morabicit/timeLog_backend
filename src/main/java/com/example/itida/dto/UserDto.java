package com.example.itida.dto;

import com.example.itida.entity.User;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class UserDto {
    Long id;
    String email;
    String fullName;
    LocalDate dateOfBirth;
    String mobileNumber;
    String role;

    public String getRole() {
        return role != null ? role : null;
    }
    public static List<UserDto> userToUserDto(List<User> users) {
        return users.stream().map(UserDto::userToUserDto).collect(Collectors.toList());
    }
    public static UserDto userToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFullName(user.getFullName());
        userDto.setEmail(user.getEmail());
        userDto.setDateOfBirth(user.getDateOfBirth());
        userDto.setMobileNumber(user.getMobileNumber());
        userDto.setRole(String.valueOf(user.getRole()));
        return userDto;
    }
}
