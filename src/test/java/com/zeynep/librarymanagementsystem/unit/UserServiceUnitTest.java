package com.zeynep.librarymanagementsystem.unit;



import com.zeynep.librarymanagementsystem.dto.UserDTO;
import com.zeynep.librarymanagementsystem.exception.EmailAlreadyExistsException;
import com.zeynep.librarymanagementsystem.exception.UserNotFoundException;
import com.zeynep.librarymanagementsystem.mapper.UserMapper;
import com.zeynep.librarymanagementsystem.model.Role;
import com.zeynep.librarymanagementsystem.model.User;
import com.zeynep.librarymanagementsystem.repository.UserRepository;
import com.zeynep.librarymanagementsystem.service.iml.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceUnitTest {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceUnitTest.class);

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRegisterUser_whenValidInput() {
        logger.info("Starting test: shouldRegisterUser_whenValidInput");
        UserDTO dto = new UserDTO(null, "user", "user@example.com",  Role.PATRON,"12345","pass123");
        User entity = new User(null, "user", "user@example.com", "encodedPass", Role.PATRON, "12345", null);
        User saved = new User(1L, "user", "user@example.com", "encodedPass", Role.PATRON, "12345", null);
        UserDTO savedDTO = new UserDTO(1L, "user", "user@example.com", Role.PATRON, "12345","encodedPass");

        logger.info("Mocking repository and mapper responses...");
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(entity);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPass");
        when(userRepository.save(entity)).thenReturn(saved);
        when(userMapper.toDTO(saved)).thenReturn(savedDTO);

        logger.info("Calling registerUser...");
        UserDTO result = userService.registerUser(dto);

        assertEquals(1L, result.getId());
        verify(userRepository).save(any(User.class));
        logger.info("Test passed: shouldRegisterUser_whenValidInput");
    }

    @Test
    void shouldThrowException_whenEmailAlreadyExists() {
        logger.info("Starting test: shouldThrowException_whenEmailAlreadyExists");
        UserDTO dto = new UserDTO(null, "user", "user@example.com", Role.PATRON, "12345","pass123");
        logger.info("UserDTO being tested: {}", dto);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
        logger.info("Calling registerUser expecting EmailAlreadyExistsException...");
        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(dto));
        logger.info("Test passed: shouldThrowException_whenEmailAlreadyExists");
    }

    @Test
    void shouldGetUserById_whenUserExists() {
        logger.info("Starting test: shouldGetUserById_whenUserExists");

        User user = new User(1L, "user", "user@example.com","pass123", Role.PATRON, "12345", null);
        UserDTO dto = new UserDTO(1L, "user", "user@example.com",  Role.PATRON, "12345","pass123");
        logger.info("UserDTO being tested: {}", dto);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(dto);

        logger.info("Calling getUserById...");
        UserDTO result = userService.getUserById(1L);
        assertEquals("user", result.getName());
        logger.info("Test passed: shouldGetUserById_whenUserExists");
    }

    @Test
    void shouldThrowException_whenUserNotFound() {
        logger.info("Starting test: shouldThrowException_whenUserNotFound");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        logger.info("Calling getUserById expecting UserNotFoundException...");
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
        logger.info("Test passed: shouldThrowException_whenUserNotFound");
    }

    @Test
    void shouldGetAllUsers_whenUsersExist() {
        logger.info("Starting test: shouldGetAllUsers_whenUsersExist");
        User user = new User(1L, "user", "user@example.com", "pass", Role.PATRON, "12345", null);
        UserDTO dto = new UserDTO(1L, "user", "user@example.com", Role.PATRON, "12345","pass123");

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDTO(user)).thenReturn(dto);

        logger.info("Calling getAllUsers...");
        List<UserDTO> users = userService.getAllUsers();
        assertEquals(1, users.size());
        logger.info("Test passed: shouldGetAllUsers_whenUsersExist");
    }

    @Test
    void shouldUpdateUser_whenValidInput() {
        logger.info("Starting test: shouldUpdateUser_whenValidInput");
        User existing = new User(1L, "Old", "old@example.com", "oldPass", Role.PATRON, "123", null);
        User updated = new User(1L, "New", "new@example.com", "encodedNew", Role.LIBRARIAN, "456", null);
        UserDTO updateDTO = new UserDTO(1L, "New", "new@example.com", Role.LIBRARIAN, "456", "newPass");
        UserDTO updatedDTO = new UserDTO(1L, "New", "new@example.com", Role.LIBRARIAN, "456", "encodedNew");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");
        when(userRepository.save(any(User.class))).thenReturn(updated);
        when(userMapper.toDTO(updated)).thenReturn(updatedDTO);

        logger.info("Calling updateUser...");
        UserDTO result = userService.updateUser(1L, updateDTO);
        assertEquals("New", result.getName());
        logger.info("Test passed: shouldUpdateUser_whenValidInput");
    }

    @Test
    void shouldDeleteUser_whenUserExists() {
        logger.info("Starting test: shouldDeleteUser_whenUserExists");
        when(userRepository.existsById(1L)).thenReturn(true);
        logger.info("Calling deleteUser...");
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
        logger.info("Test passed: shouldDeleteUser_whenUserExists");
    }

    @Test
    void shouldThrowException_whenUserNotFoundForDeletion() {
        logger.info("Starting test: shouldThrowException_whenUserNotFoundForDeletion");
        when(userRepository.existsById(999L)).thenReturn(false);
        logger.info("Calling deleteUser expecting UserNotFoundException...");
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(999L));
        logger.info("Test passed: shouldThrowException_whenUserNotFoundForDeletion");
    }
}
