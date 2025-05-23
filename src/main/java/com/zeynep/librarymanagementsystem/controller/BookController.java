package com.zeynep.librarymanagementsystem.controller;

import com.zeynep.librarymanagementsystem.dto.BookDTO;
import com.zeynep.librarymanagementsystem.mapper.BookMapper;
import com.zeynep.librarymanagementsystem.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/books")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;
    private final BookMapper bookMapper;

    public BookController(BookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }

    @Operation(summary = "Add a new book", description = "This endpoint allows you to add a new book to the library")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "409", description = "A book with the provided ISBN already exists", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "403", description = " You do not have permission to perform this action", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials", content = @Content)
    })

    @PreAuthorize("hasRole('LIBRARIAN')")
    @PostMapping
    public ResponseEntity<BookDTO> addBook(@Valid @RequestBody BookDTO bookDTO) {
        logger.info("Request to add a new book: {}", bookDTO);
        try {
            BookDTO savedBook = bookService.addBook(bookDTO);
            logger.info("Book added successfully: {}", savedBook);
            return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error adding book: {}", e.getMessage());
            throw e; // Rethrow or handle the exception accordingly
        }
    }

    @Operation(summary = "Get a book by ID", description = "Retrieve details of a specific book based on its unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(
            @Parameter(description = "ID of the book to retrieve") @PathVariable Long id) {
        logger.info("Request to retrieve book with ID: {}", id);
        try {
            BookDTO bookDTO = bookService.getBookById(id);
            if (bookDTO == null) {
                logger.warn("Book with ID {} not found", id);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            logger.info("Book found: {}", bookDTO);
            return new ResponseEntity<>(bookDTO, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving book: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Get all books", description = "Retrieve a list of all books in the library")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials", content = @Content)

    })
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        logger.info("Request to retrieve all books");
        try {
            List<BookDTO> books = bookService.getAllBooks();
            logger.info("Successfully retrieved {} books", books.size());
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving books: {}", e.getMessage());
            throw e;
        }
    }


    @Operation(summary = "Update a book", description = "Update details of an existing book by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "403", description = " You do not have permission to perform this action", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials", content = @Content)

    })
    @PreAuthorize("hasRole('LIBRARIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(
            @Parameter(description = "ID of the book to update") @PathVariable Long id,
            @RequestBody BookDTO bookDTO) {
        logger.info("Request to update book with ID: {}", id);
        try {
            BookDTO updatedBook = bookService.updateBook(id, bookDTO);
            logger.info("Book updated successfully: {}", updatedBook);
            return new ResponseEntity<>(updatedBook, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating book: {}", e.getMessage());
            throw e;
        }
    }
    @Operation(
            summary = "Search books",
            description = "Search for books by title, author, ISBN, or genre with pagination support",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved search results",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = BookDTO.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials", content = @Content)

            }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<BookDTO>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        logger.info("Request to search books with keyword: {}, page: {}, size: {}", keyword, page, size);
        try {
            Page<BookDTO> results = bookService.searchBooks(keyword, page, size);

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error searching books: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Delete a book", description = "Remove a book from the library by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "403", description = " You do not have permission to perform this action", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required - Missing or invalid credentials", content = @Content)

    })
    @PreAuthorize("hasRole('LIBRARIAN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID of the book to delete") @PathVariable Long id) {
        logger.info("Request to delete book with ID: {}", id);
        try {
            bookService.deleteBook(id);
            logger.info("Book with ID {} deleted successfully", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error deleting book: {}", e.getMessage());
            throw e;
        }
    }

}
