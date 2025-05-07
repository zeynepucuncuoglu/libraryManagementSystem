package com.zeynep.librarymanagementsystem.controller;

import com.zeynep.librarymanagementsystem.dto.AuthRequest;
import com.zeynep.librarymanagementsystem.dto.UserDTO;
import com.zeynep.librarymanagementsystem.security.JwtService;
import com.zeynep.librarymanagementsystem.service.UserService;
import com.zeynep.librarymanagementsystem.service.iml.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful authentication. Returns JWT token",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"token\": \"<jwt_token>\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(), authRequest.getPassword()));

        UserDetails user = customUserDetailsService.loadUserByUsername(authRequest.getEmail());
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(Map.of("token", token));
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or bad input",
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid  @RequestBody UserDTO userDTO) {
        UserDTO savedUserDTO = userService.registerUser(userDTO);
        return new ResponseEntity<>(savedUserDTO, HttpStatus.CREATED);
    }
}
