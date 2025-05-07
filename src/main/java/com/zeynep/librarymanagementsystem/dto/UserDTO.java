package com.zeynep.librarymanagementsystem.dto;

import com.zeynep.librarymanagementsystem.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object representing a user")
public class UserDTO {

    @Schema(description = "Unique identifier of the user", example = "3")
    private Long id;

    @Schema(description = "Full name of the user", example = "Jane Doe")
    private String name;

    @Schema(description = "Email address of the user", example = "jane.doe@example.com")
    private String email;

    @Schema(description = "Role of the user", example = "PATRON")
    private Role role;

    @Schema(description = "Contact information (phone or address)", example = "+1-234-567-8901")
    private String contactInfo;

    @Schema(description = "Password for authentication", example = "securePassword123")
    private String password;
}
