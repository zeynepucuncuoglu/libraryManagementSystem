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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

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
        logger.info("Attempting to register user with email: {}", userDTO.getEmail());
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            logger.error("Email already exists: {}", userDTO.getEmail());
            throw new EmailAlreadyExistsException("A User with the Email " + userDTO.getEmail() + " already exists.");
        }
        // Convert DTO to Entity
        User user = userMapper.toEntity(userDTO);
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Save user to repository
        User savedUser = userRepository.save(user);
        // Convert saved entity to DTO and return
        logger.info("Successfully registered user with email: {}", savedUser.getEmail());

        return userMapper.toDTO(savedUser);
    }

    @Override
    public UserDTO getUserById(Long id) {
        logger.info("Attempting to retrieve user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User with ID {} not found", id);
                    return new UserNotFoundException("User with ID " + id + " not found.");
                });
        logger.info("Successfully retrieved user with ID: {}", id);
        return userMapper.toDTO(user);

    }

    @Override
    public List<UserDTO> getAllUsers() {
        logger.info("Attempting to retrieve all users");
        List<UserDTO> users = userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        logger.info("Successfully retrieved {} users", users.size());
        return users;
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO updatedUserDTO) {
        logger.info("Attempting to update user with ID: {}", id);

        return userRepository.findById(id).map(user -> {
            logger.info("Updating user with ID: {}. New details: Name: {}, Email: {}, Role: {}, ContactInfo: {}",
                    id, updatedUserDTO.getName(), updatedUserDTO.getEmail(), updatedUserDTO.getRole(), updatedUserDTO.getContactInfo());

            // Map DTO to Entity to update
            user.setName(updatedUserDTO.getName());
            user.setEmail(updatedUserDTO.getEmail());
            user.setRole(updatedUserDTO.getRole());
            user.setContactInfo(updatedUserDTO.getContactInfo());
            if (updatedUserDTO.getPassword() != null && !updatedUserDTO.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updatedUserDTO.getPassword()));
            }
            User updatedUser = userRepository.save(user);
            logger.info("Successfully updated user with ID: {}", id);
            // Map updated entity back to DTO
            return userMapper.toDTO(updatedUser);
        }).orElseThrow(() -> {
            logger.error("User with ID {} not found for update", id);
            return new UserNotFoundException("User with ID " + id + " not found.");
        });
    }

    @Override
    public void deleteUser(Long id) {
        logger.info("Attempting to delete user with ID: {}", id);
        if(!userRepository.existsById(id)) {
            logger.error("User with ID {} not found for deletion", id);
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }
        userRepository.deleteById(id);
        logger.info("Successfully deleted user with ID: {}", id);
    }
}