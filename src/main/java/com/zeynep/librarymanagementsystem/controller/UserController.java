package com.zeynep.librarymanagementsystem.controller;

import com.zeynep.librarymanagementsystem.dto.UserDTO;
import com.zeynep.librarymanagementsystem.mapper.UserMapper;
import com.zeynep.librarymanagementsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "Operations related to user management")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }



    @Operation(
            summary = "Get user by ID",
            description = "Fetches a user by their unique identifier"
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID of the user to fetch") @PathVariable Long id
    ) {
        UserDTO userDTO = userService.getUserById(id);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all registered users"
    )
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(
            summary = "Update user details",
            description = "Updates an existing user's information"
    )
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID of the user to update") @PathVariable Long id,
            @Parameter(description = "Updated user details") @RequestBody UserDTO userDTO
    ) {
        UserDTO updatedUserDTO = userService.updateUser(id, userDTO);
        return new ResponseEntity<>(updatedUserDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete user",
            description = "Deletes a user from the system by ID"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable Long id
    ) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
