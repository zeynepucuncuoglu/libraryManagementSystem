package com.zeynep.librarymanagementsystem.service.iml;

import com.zeynep.librarymanagementsystem.dto.UserDTO;
import com.zeynep.librarymanagementsystem.exception.EmailAlreadyExistsException;
import com.zeynep.librarymanagementsystem.exception.ISBNAlreadyExistsException;
import com.zeynep.librarymanagementsystem.exception.UserNotFoundException;
import com.zeynep.librarymanagementsystem.mapper.UserMapper;
import com.zeynep.librarymanagementsystem.model.Book;
import com.zeynep.librarymanagementsystem.model.User;
import com.zeynep.librarymanagementsystem.repository.UserRepository;
import com.zeynep.librarymanagementsystem.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }


    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A User with the Email " + userDTO.getEmail() + " already exists.");
        }
        // Convert DTO to Entity
        User user = userMapper.toEntity(userDTO);
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Save user to repository
        User savedUser = userRepository.save(user);
        // Convert saved entity to DTO and return
        return userMapper.toDTO(savedUser);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found."));
        return userMapper.toDTO(user);

    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)  // Convert each entity to DTO
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO updatedUserDTO) {

        return userRepository.findById(id).map(user -> {
            // Map DTO to Entity to update

            user.setName(updatedUserDTO.getName());
            user.setEmail(updatedUserDTO.getEmail());
            user.setRole(updatedUserDTO.getRole());
            user.setContactInfo(updatedUserDTO.getContactInfo());
            if (updatedUserDTO.getPassword() != null && !updatedUserDTO.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updatedUserDTO.getPassword()));
            }
            User updatedUser = userRepository.save(user);
            // Map updated entity back to DTO
            return userMapper.toDTO(updatedUser);
        }).orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found."));
    }

    @Override
    public void deleteUser(Long id) {
        if(!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }
        userRepository.deleteById(id);
    }
}