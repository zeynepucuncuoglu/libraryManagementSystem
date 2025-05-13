package com.zeynep.librarymanagementsystem.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeynep.librarymanagementsystem.dto.BorrowRecordDTO;
import com.zeynep.librarymanagementsystem.exception.ActiveBorrowRecordNotFoundException;
import com.zeynep.librarymanagementsystem.exception.BookNotAvailableException;
import com.zeynep.librarymanagementsystem.exception.BookNotFoundException;
import com.zeynep.librarymanagementsystem.exception.UserNotFoundException;
import com.zeynep.librarymanagementsystem.model.Role;
import com.zeynep.librarymanagementsystem.model.User;
import com.zeynep.librarymanagementsystem.repository.BorrowRecordRepository;
import com.zeynep.librarymanagementsystem.repository.UserRepository;
import com.zeynep.librarymanagementsystem.repository.BookRepository;
import com.zeynep.librarymanagementsystem.security.JwtService;
import com.zeynep.librarymanagementsystem.service.BookService;
import com.zeynep.librarymanagementsystem.service.BorrowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
class BorrowControllerIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(BorrowControllerIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private BorrowService borrowService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowRecordRepository borrowRepository;

    @Autowired
    private BookRepository bookRepository;


    private String librarianToken;
    private User librarianUser;

    private User patronUser;
    private  String patronToken;

    @BeforeEach
    void setUp() {
        borrowRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        librarianUser = new User();
        librarianUser.setName("Librarian");
        librarianUser.setEmail("librarian@example.com");
        librarianUser.setPassword("Password124");
        librarianUser.setRole(Role.LIBRARIAN);

        librarianUser = userRepository.save(librarianUser);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(librarianUser.getEmail())
                .password(librarianUser.getPassword())
                .roles(librarianUser.getRole().name())
                .build();

        librarianToken = jwtService.generateToken(userDetails);

        patronUser = new User();
        patronUser.setName("Patron");
        patronUser.setEmail("patron@example.com");
        patronUser.setPassword("Password124");
        patronUser.setRole(Role.PATRON);

        patronUser = userRepository.save(patronUser);

        UserDetails patronUserDetails  = org.springframework.security.core.userdetails.User
                .withUsername(patronUser.getEmail())
                .password(patronUser.getPassword())
                .roles(patronUser.getRole().name())
                .build();


        patronToken = jwtService.generateToken(patronUserDetails);
    }

    @Test
    void shouldBorrowBook_whenValidRequest() throws Exception {
        logger.info("Starting Test: shouldBorrowBook_whenValidRequest");
        // Mock successful borrowBook
        BorrowRecordDTO borrowRecordDTO = BorrowRecordDTO.builder()
                .userId(1L)
                .bookId(42L)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusWeeks(2))
                .build();

        when(borrowService.borrowBook(1L, 42L)).thenReturn(borrowRecordDTO);

        logger.debug("Created borrowRecordDTO: {}", borrowRecordDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/borrow/1/42")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.bookId").value(42L))
                .andExpect(jsonPath("$.borrowDate").exists())
                .andExpect(jsonPath("$.dueDate").exists());
        logger.info("Completed Test: shouldBorrowBook_whenValidRequest");
    }

    @Test
    void shouldFailBorrow_whenUserNotFound() throws Exception {
        logger.info("Starting Test: shouldFailBorrow_whenUserNotFound");
        // Simulate that the user does not exist
        when(borrowService.borrowBook(1L, 42L)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/borrow/1/42")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        logger.info("Completed Test: shouldFailBorrow_whenUserNotFound");
    }

    @Test
    void shouldFailBorrow_whenBookNotFound() throws Exception {
        logger.info("Starting Test: shouldFailBorrow_whenBookNotFound");
        // Simulate that the book does not exist
        when(borrowService.borrowBook(1L, 42L)).thenThrow(new BookNotFoundException("Book not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/borrow/1/42")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        logger.info("Completed Test: shouldFailBorrow_whenBookNotFound");

    }

    @Test
    void shouldFailBorrow_whenBookNotAvailable() throws Exception {
        logger.info("Starting Test: shouldFailBorrow_whenBookNotAvailable");
        // Simulate that the book is not available for borrowing
        when(borrowService.borrowBook(1L, 42L)).thenThrow(new BookNotAvailableException("Book is not available for borrowing"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/borrow/1/42")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        logger.info("Completed Test: shouldFailBorrow_whenBookNotAvailable");

    }

    @Test
    void shouldFailBorrow_whenUnauthorized() throws Exception {
        logger.info("Starting Test: shouldFailBorrow_whenUnauthorized");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/borrow/1/42")
                        .header("Authorization", "Bearer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        logger.info("Completed Test: shouldFailBorrow_whenUnauthorized");

    }




    @Test
    void shouldReturnBook_whenValidRequest() throws Exception {
        logger.info("Running: shouldReturnBook_whenValidRequest test");
        BorrowRecordDTO borrowRecordDTO = BorrowRecordDTO.builder()
                .userId(1L)
                .bookId(42L)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusWeeks(2))
                .returnDate(LocalDate.now())
                .build();

        when(borrowService.returnBook(1L, 42L)).thenReturn(borrowRecordDTO);
        logger.debug("Created borrowRecordDTO: {}", borrowRecordDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/borrow/return/1/42")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.bookId").value(42L))
                .andExpect(jsonPath("$.returnDate").exists());
        logger.info("Passed: Successfully returned book returned.");
    }


    @Test
    void shouldFailReturn_whenBookNotBorrowed() throws Exception {
        logger.info("Running: shouldFailReturn_whenBookNotBorrowed ");
        when(borrowService.returnBook(1L, 42L)).thenThrow(new ActiveBorrowRecordNotFoundException("Book was not borrowed or already returned"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/borrow/return/1/42")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        logger.info("Passed: Correctl received 404 Book was not borrowed or already returned.");

    }

    @Test
    void shouldFailReturn_whenUnauthorized() throws Exception {
        logger.info("Running: shouldFailReturn_whenUnauthorized");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/borrow/return/1/42")
                        .header("Authorization", "Bearer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        logger.info("Passed: Correctly received 401 Unauthorized access for missing token.");
    }




    @Test
    void shouldGetUserBorrowHistory_whenValidRequest() throws Exception {
        logger.info("Running: shouldGetUserBorrowHistory_whenValidRequest");
        BorrowRecordDTO borrowRecordDTO = BorrowRecordDTO.builder()
                .userId(1L)
                .bookId(42L)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusWeeks(2))
                .build();

        when(borrowService.getUserBorrowHistory(1L)).thenReturn(List.of(borrowRecordDTO));
        logger.debug("Created borrowRecordDTO: {}", borrowRecordDTO);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/borrow/user/1")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].bookId").value(42L))
                .andExpect(jsonPath("$[0].borrowDate").exists())
                .andExpect(jsonPath("$[0].dueDate").exists());
        logger.info("Passed: Successfully returned user borrow history.");
    }

    @Test
    void shouldFailGetUserBorrowHistory_whenUserNotFound() throws Exception {
        logger.info("Running: shouldFailGetUserBorrowHistory_whenUserNotFound ");
        when(borrowService.getUserBorrowHistory(1L)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/borrow/user/1")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
        logger.info("Passed: Correctly received 404 user not found with invalid ID.");
    }

    @Test
    void shouldFailGetUserBorrowHistory_whenUnauthorized() throws Exception {
        logger.info("Running: shouldFailGetUserBorrowHistory_whenUnauthorized");
        mockMvc.perform(MockMvcRequestBuilders.get("/api/borrow/user/1")
                        .header("Authorization", "Bearer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        logger.info("Passed: Correctly received 401 Unauthorized access for missing token.");
    }



    @Test
    void shouldGetAllBorrowHistory_whenLibrarianRequest() throws Exception {
        logger.info("Running: shouldGetAllBorrowHistory_whenLibrarianRequest");

        BorrowRecordDTO borrowRecordDTO = BorrowRecordDTO.builder()
                .userId(1L)
                .bookId(42L)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusWeeks(2))
                .build();

        when(borrowService.getAllBorrowHistory()).thenReturn(List.of(borrowRecordDTO));
        logger.debug("Created borrowRecordDTO: {}", borrowRecordDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/borrow/history")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].bookId").value(42L))
                .andExpect(jsonPath("$[0].borrowDate").exists())
                .andExpect(jsonPath("$[0].dueDate").exists());
        logger.info("Passed: Successfully returned book history.");
    }

    @Test
    void shouldFailGetAllBorrowHistory_whenUserIsNotLibrarian() throws Exception {
        logger.info("Running: shouldFailGetAllBorrowHistory_whenUserIsNotLibrarian");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/borrow/history")
                        .header("Authorization", "Bearer " + patronToken) // Patron instead of Librarian
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        logger.info("Passed: Correctly received 403 forbidden access when user not librarian.");
    }

    @Test
    void shouldFailGetAllBorrowHistory_whenUnauthorized() throws Exception {
        logger.info("Running: shouldFailGetAllBorrowHistory_whenUnauthorized");
        mockMvc.perform(MockMvcRequestBuilders.get("/api/borrow/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        logger.info("Passed:  Correctly received 401 unauthorized for missing token.");
    }



    @Test
    void shouldFailGetOverdueBooks_whenUnauthorized() throws Exception {
        logger.info("Running: shouldFailGetOverdueBooks_whenUnauthorized");
        mockMvc.perform(MockMvcRequestBuilders.get("/api/borrow/overdue")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        logger.info("Passed: Correctly received 401 unauthorized for missing token.");
    }

    @Test
    void shouldFailGetOverdueBooks_whenUserIsNotLibrarian() throws Exception {
        logger.info("Running: shouldFailGetOverdueBooks_whenUserIsNotLibrarian");

        // Non-librarian user
        mockMvc.perform(MockMvcRequestBuilders.get("/api/borrow/overdue")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        logger.info("Test passed: Correctly received 403 forbidden access when user not librarian.");
    }

    @Test
    void shouldGetOverdueBooks_whenRequestIsValidAndUserIsLibrarian() throws Exception {
        logger.info("Running: shouldGetOverdueBooks_whenRequestIsValidAndUserIsLibrarian");

        BorrowRecordDTO borrowRecordDTO = BorrowRecordDTO.builder()
                .userId(1L)
                .bookId(42L)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().minusDays(1))  // Overdue
                .build();

        when(borrowService.getOverdueBooks()).thenReturn(List.of(borrowRecordDTO));

        logger.debug("Created borrowRecordDTO: {}", borrowRecordDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/borrow/overdue")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].bookId").value(42L))
                .andExpect(jsonPath("$[0].borrowDate").exists())
                .andExpect(jsonPath("$[0].dueDate").exists());
        logger.info("Passed: Successfully get overdue book list.");
    }
}
