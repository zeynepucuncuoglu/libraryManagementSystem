package com.zeynep.librarymanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data Transfer Object representing a book")
public class BookDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Title must not be Empty")
    @Size(min = 1, max = 255)
    @Schema(description = "Title of the book", example = "The Great Gatsby")
    private String title;

    @NotBlank(message = "Author must not be empty")
    @Size(min = 1, max = 255)
    @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
    private String author;

    @NotBlank
    @Pattern(regexp = "^(97(8|9))?\\d{9}(\\d|X)$", message = "Invalid ISBN format")
    @Schema(description = "ISBN number of the book", example = "9780743273565")
    private String isbn;

    @NotNull(message = "Publication date is required")
    @Schema(description = "Publication date of the book", example = "1925-04-10")
    private LocalDate publicationDate;

    @NotBlank(message = "Genre must not be empty")
    @Size(min = 1, max = 100)
    @Schema(description = "Genre of the book", example = "Fiction")
    private String genre;

    @NotNull
    @Schema(description = "Availability status of the book", example = "true")
    private boolean available;
}
