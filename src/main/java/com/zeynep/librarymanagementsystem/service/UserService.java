package com.zeynep.librarymanagementsystem.service;

import com.zeynep.librarymanagementsystem.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserService  {
    UserDTO registerUser(UserDTO userDTO);
    UserDTO getUserById(Long id);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UserDTO updatedUserDTO);
    void deleteUser(Long id);

}

