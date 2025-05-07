package com.zeynep.librarymanagementsystem.service.iml;

import com.zeynep.librarymanagementsystem.dto.UserDTO;
import com.zeynep.librarymanagementsystem.mapper.UserMapper;
import com.zeynep.librarymanagementsystem.model.Book;
import com.zeynep.librarymanagementsystem.model.User;
import com.zeynep.librarymanagementsystem.repository.UserRepository;
import com.zeynep.librarymanagementsystem.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
                .orElseThrow(() -> new RuntimeException("User not found"));
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
        // Get the current logged-in user from the context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserRole = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .findFirst().orElse("");

        // Check if the current user is a librarian
        if (!currentUserRole.equals("ROLE_LIBRARIAN")) {
            throw new RuntimeException("Only librarians can update user information.");
        }

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
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}