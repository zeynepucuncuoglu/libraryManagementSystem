package com.zeynep.librarymanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zeynep.librarymanagementsystem.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object representing a user")
public class UserDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Name must not be Empty")
    @Size(min = 1, max = 255)
    @Schema(description = "Full name of the user", example = "Jane Doe")
    private String name;

    @NotBlank(message = "Email must not be empty")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    @Schema(description = "Email address of the user", example = "jane.doe@example.com")
    private String email;

    @NotNull(message = "Role must not be null")
    @Schema(description = "Role of the user", example = "PATRON")
    private Role role;

    @Size(max = 255, message = "Contact info must be less than 255 characters")
    @Schema(description = "Contact information (phone or address)", example = "+1-234-567-8901")
    private String contactInfo;

    @NotBlank(message = "Password must not be empty")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    @Schema(description = "Password for authentication", example = "securePassword123")
    private String password;
}
