package com.zeynep.librarymanagementsystem.controller;

import com.zeynep.librarymanagementsystem.dto.BorrowRecordDTO;
import com.zeynep.librarymanagementsystem.service.BorrowService;
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
@RequestMapping("/api/borrow")
@Tag(name = "Borrow Controller", description = "Operations related to borrowing and returning books")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @Operation(
            summary = "Borrow a book",
            description = "Allows a user to borrow a book by providing the user ID and book ID"
    )
    @PostMapping("/{userId}/{bookId}")
    public ResponseEntity<BorrowRecordDTO> borrowBook(
            @Parameter(description = "ID of the user borrowing the book") @PathVariable Long userId,
            @Parameter(description = "ID of the book to be borrowed") @PathVariable Long bookId
    ) {
        BorrowRecordDTO borrowRecordDTO = borrowService.borrowBook(userId, bookId);
        return new ResponseEntity<>(borrowRecordDTO, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Return a borrowed book",
            description = "Allows a user to return a previously borrowed book"
    )
    @PostMapping("/return/{userId}/{bookId}")
    public ResponseEntity<BorrowRecordDTO> returnBook(
            @Parameter(description = "ID of the user returning the book") @PathVariable Long userId,
            @Parameter(description = "ID of the book to be returned") @PathVariable Long bookId
    ) {
        BorrowRecordDTO borrowRecordDTO = borrowService.returnBook(userId, bookId);
        return new ResponseEntity<>(borrowRecordDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Get borrow history of a user",
            description = "Retrieves all borrow records associated with a specific user"
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BorrowRecordDTO>> getUserBorrowHistory(
            @Parameter(description = "ID of the user") @PathVariable Long userId
    ) {
        List<BorrowRecordDTO> borrowHistory = borrowService.getUserBorrowHistory(userId);
        return new ResponseEntity<>(borrowHistory, HttpStatus.OK);
    }

    @Operation(
            summary = "Get all borrow records",
            description = "Retrieves the complete borrow history. Typically used by admins."
    )
    @GetMapping("/history")
    public ResponseEntity<List<BorrowRecordDTO>> getAllBorrowHistory() {
        List<BorrowRecordDTO> borrowHistory = borrowService.getAllBorrowHistory();
        return new ResponseEntity<>(borrowHistory, HttpStatus.OK);
    }

    @Operation(
            summary = "Get overdue borrowed books",
            description = "Retrieves a list of all overdue borrow records"
    )
    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowRecordDTO>> getOverdueBooks() {
        List<BorrowRecordDTO> overdueBooks = borrowService.getOverdueBooks();
        return new ResponseEntity<>(overdueBooks, HttpStatus.OK);
    }
}
