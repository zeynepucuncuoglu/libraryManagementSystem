package com.zeynep.librarymanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data Transfer Object representing a borrow record")
public class BorrowRecordDTO {

    @Schema(description = "Unique identifier of the borrow record", example = "1001")
    private Long id;

    @Schema(description = "ID of the user who borrowed the book", example = "5")
    private Long userId;

    @Schema(description = "ID of the book that was borrowed", example = "42")
    private Long bookId;

    @Schema(description = "Date when the book was borrowed", example = "2025-05-01")
    private LocalDate borrowDate;

    @Schema(description = "Due date for returning the book", example = "2025-05-15")
    private LocalDate dueDate;

    @Schema(description = "Date when the book was returned", example = "2025-05-10", nullable = true)
    private LocalDate returnDate;
}
