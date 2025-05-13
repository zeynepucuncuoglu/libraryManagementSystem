package com.zeynep.librarymanagementsystem.controller;

import com.zeynep.librarymanagementsystem.dto.BookDTO;
import com.zeynep.librarymanagementsystem.dto.UserDTO;
import com.zeynep.librarymanagementsystem.mapper.UserMapper;
import com.zeynep.librarymanagementsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "Operations related to user management")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "403", description = " You do not have permission to perform this action", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials", content = @Content)
    })
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID of the user to fetch") @PathVariable Long id
    ) {
        logger.info("Fetching user with ID {}", id);
        UserDTO userDTO = userService.getUserById(id);
        if (userDTO != null) {
            logger.info("User with ID {} found", id);
        } else {
            logger.warn("User with ID {} not found", id);
        }
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all registered users"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "403", description = " You do not have permission to perform this action", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials", content = @Content)

    })
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("Retrieving all users");
        List<UserDTO> users = userService.getAllUsers();
        logger.info("Successfully retrieved {} users", users.size());
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(
            summary = "Update user details",
            description = "Updates an existing user's information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "403", description = " You do not have permission to perform this action", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials", content = @Content)

    })
    @PreAuthorize("hasRole('LIBRARIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID of the user to update") @PathVariable Long id,
            @Parameter(description = "Updated user details") @Valid @RequestBody UserDTO userDTO
    ) {
        logger.info("Updating user with ID {}", id);
        UserDTO updatedUserDTO = userService.updateUser(id, userDTO);
        logger.info("User with ID {} successfully updated", id);
        return new ResponseEntity<>(updatedUserDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete user",
            description = "Deletes a user from the system by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "403", description = " You do not have permission to perform this action", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials", content = @Content)

    })
    @PreAuthorize("hasRole('LIBRARIAN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable Long id
    ) {
        logger.info("Attempting to delete user with ID {}", id);
        userService.deleteUser(id);
        logger.info("User with ID {} deleted successfully", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
