package com.zeynep.librarymanagementsystem.controller;

import com.zeynep.librarymanagementsystem.dto.BorrowRecordDTO;
import com.zeynep.librarymanagementsystem.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/borrow")
@Tag(name = "Borrow Controller", description = "Operations related to borrowing and returning books")
public class BorrowController {
    private static final Logger logger = LoggerFactory.getLogger(BorrowController.class);

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @Operation(
            summary = "Borrow a book",
            description = "Allows a user to borrow a book by providing the user ID and book ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book successfully borrowed"),
            @ApiResponse(responseCode = "404", description = "User or Book not found"),
            @ApiResponse(responseCode = "400", description = "Book is not available for borrowing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PostMapping("/{userId}/{bookId}")
    public ResponseEntity<BorrowRecordDTO> borrowBook(
            @Parameter(description = "ID of the user borrowing the book") @PathVariable Long userId,
            @Parameter(description = "ID of the book to be borrowed") @PathVariable Long bookId
    ) {
        logger.info("Attempting to borrow book with ID {} for user with ID {}", bookId, userId);
        BorrowRecordDTO borrowRecordDTO = borrowService.borrowBook(userId, bookId);
        logger.info("Book with ID {} successfully borrowed by user with ID {}", bookId, userId);
        return new ResponseEntity<>(borrowRecordDTO, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Return a borrowed book",
            description = "Allows a user to return a previously borrowed book"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book successfully returned"),
            @ApiResponse(responseCode = "404", description = "Book was not borrowed or already returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials")
    })
    @PostMapping("/return/{userId}/{bookId}")
    public ResponseEntity<BorrowRecordDTO> returnBook(
            @Parameter(description = "ID of the user returning the book") @PathVariable Long userId,
            @Parameter(description = "ID of the book to be returned") @PathVariable Long bookId
    ) {
        logger.info("Attempting to return book with ID {} by user with ID {}", bookId, userId);
        BorrowRecordDTO borrowRecordDTO = borrowService.returnBook(userId, bookId);
        logger.info("Book with ID {} successfully returned by user with ID {}", bookId, userId);
        return new ResponseEntity<>(borrowRecordDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Get borrow history of a user",
            description = "Retrieves all borrow records associated with a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrow history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BorrowRecordDTO>> getUserBorrowHistory(
            @Parameter(description = "ID of the user") @PathVariable Long userId
    ) {
        logger.info("Retrieving borrow history for user with ID {}", userId);
        List<BorrowRecordDTO> borrowHistory = borrowService.getUserBorrowHistory(userId);
        logger.info("Successfully retrieved borrow history for user with ID {}", userId);
        return new ResponseEntity<>(borrowHistory, HttpStatus.OK);
    }

    @Operation(
            summary = "Get all borrow records",
            description = "Retrieves the complete borrow history. Typically used by admins."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All borrow records retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - only librarians allowed"),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials")
    })
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/history")
    public ResponseEntity<List<BorrowRecordDTO>> getAllBorrowHistory() {
        logger.info("Librarian requested all borrow records");
        List<BorrowRecordDTO> borrowHistory = borrowService.getAllBorrowHistory();
        logger.info("Successfully retrieved all borrow records");
        return new ResponseEntity<>(borrowHistory, HttpStatus.OK);
    }

    @Operation(
            summary = "Get overdue borrowed books",
            description = "Retrieves a list of all overdue borrow records"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue books retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - only librarians allowed"),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials")
    })
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowRecordDTO>> getOverdueBooks() {
        logger.info("Librarian requested overdue books list");
        List<BorrowRecordDTO> overdueBooks = borrowService.getOverdueBooks();
        logger.info("Successfully retrieved overdue books list");
        return new ResponseEntity<>(overdueBooks, HttpStatus.OK);
    }
}
