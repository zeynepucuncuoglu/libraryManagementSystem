package com.zeynep.librarymanagementsystem.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeynep.librarymanagementsystem.dto.BookDTO;
import com.zeynep.librarymanagementsystem.exception.BookNotFoundException;
import com.zeynep.librarymanagementsystem.exception.ISBNAlreadyExistsException;
import com.zeynep.librarymanagementsystem.model.Book;
import com.zeynep.librarymanagementsystem.model.Role;

import com.zeynep.librarymanagementsystem.model.User;
import com.zeynep.librarymanagementsystem.repository.BookRepository;
import com.zeynep.librarymanagementsystem.repository.BorrowRecordRepository;
import com.zeynep.librarymanagementsystem.repository.UserRepository;
import com.zeynep.librarymanagementsystem.security.CustomUserDetails;
import com.zeynep.librarymanagementsystem.security.JwtService;
import com.zeynep.librarymanagementsystem.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
public class BookControllerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(BookControllerIntegrationTest.class);


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

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
    @WithMockUser(username = "Librarian", roles = "LIBRARIAN")
    void shouldSaveBook_whenRequestIsValidAndUserIsLibrarian() throws Exception {
        logger.info("Starting Test: shouldSaveBook_whenRequestIsValidAndUserIsLibrarian");
        // Setup the BookDTO
        BookDTO bookDTO = BookDTO.builder()
                .title("The Catcher in the Rye")
                .author("J.D. Salinger")
                .isbn("9780316769488")
                .publicationDate(LocalDate.of(1951, 7, 16))
                .genre("Fiction")
                .available(true)
                .build();
        logger.debug("Created bookDTO: {}", bookDTO);

        // Mock the service call
        when(bookService.addBook(any(BookDTO.class))).thenReturn(bookDTO);

        // Perform the POST request and validate the response
        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + librarianToken) // Pass the valid token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("The Catcher in the Rye"))
                .andExpect(jsonPath("$.author").value("J.D. Salinger"))
                .andExpect(jsonPath("$.isbn").value("9780316769488"));
        logger.info("Completed Test: shouldSaveBook_whenRequestIsValidAndUserIsLibrarian");
    }

    @Test
    void shouldFailToSaveBook_whenAuthorizationTokenIsMissing() throws Exception {
        logger.info("Starting Test: shouldFailToSaveBook_whenAuthorizationTokenIsMissing");

        BookDTO bookDTO = BookDTO.builder()
                .title("The Catcher in the Rye")
                .author("J.D. Salinger")
                .isbn("9780316769488")
                .publicationDate(LocalDate.of(1951, 7, 16))
                .genre("Fiction")
                .available(true)
                .build();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isUnauthorized());
        logger.info("Completed Test: shouldFailToSaveBook_whenAuthorizationTokenIsMissing");
    }

    @Test
    void shouldFailToSaveBook_whenBookDataIsInvalid() throws Exception {
        logger.info("Starting Test: shouldFailToSaveBook_whenBookDataIsInvalid");
        // Create an invalid BookDTO with missing required fields or incorrect data
        BookDTO invalidBookDTO = BookDTO.builder()
                .title("The Catcher in the Rye")
                .author("") // Invalid author
                .isbn("9780316769488") // Valid ISBN
                .publicationDate(null) // Invalid date
                .genre("Fiction")
                .available(true)
                .build();

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookDTO)))
                .andExpect(status().isBadRequest()); // Expecting 400 Bad Request
        logger.info("Completed Test: shouldFailToSaveBook_whenBookDataIsInvalid");
    }

    @Test
    @WithMockUser(username = "Patron", roles = "PATRON")
    void shouldFailToSaveBook_whenUserIsNotLibrarian() throws Exception {
        logger.info("Starting Test: shouldFailToSaveBook_whenUserIsNotLibrarian");
        BookDTO bookDTO = BookDTO.builder()
                .title("The Catcher in the Rye")
                .author("J.D. Salinger")
                .isbn("9780316769488")
                .publicationDate(LocalDate.of(1951, 7, 16))
                .genre("Fiction")
                .available(true)
                .build();

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer" + patronToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isForbidden());
        logger.info("Completed Test: shouldFailToSaveBook_whenUserIsNotLibrarian");
    }


    @Test
    @WithMockUser(username = "Librarian", roles = "LIBRARIAN")
    void shouldFailToSaveBook_whenISBNisDuplicate() throws Exception {
        logger.info("Starting Test: shouldFailToSaveBook_whenISBNisDuplicate");
        // Setup the BookDTO
        BookDTO bookDTO = BookDTO.builder()
                .title("Original Book")
                .author("Author A")
                .isbn("9780316769488")
                .publicationDate(LocalDate.of(2020, 1, 1))
                .genre("Fiction")
                .available(true)
                .build();


        // Simulate duplicate ISBN in the service for the second POST request
        when(bookService.addBook(any(BookDTO.class)))
                .thenThrow(new ISBNAlreadyExistsException("ISBN already exists")); // Assuming you have this exception handling

        // Second POST request with the same ISBN - should return 409 Conflict
        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isConflict());
        logger.info("Completed Test: shouldFailToSaveBook_whenISBNisDuplicate");
    }


    @Test
    @WithMockUser(username = "Librarian", roles = "LIBRARIAN")
    void shouldGetBookByID_whenRequestIsValid() throws Exception {
        logger.info("Starting Test: shouldGetBookByID_whenRequestIsValid");
        // Setup the BookDTO
        BookDTO bookDTO = BookDTO.builder()
                .title("1984")
                .author("George Orwell")
                .isbn("9780451524935")
                .publicationDate(LocalDate.of(1949, 6, 8))
                .genre("Dystopian")
                .available(true)
                .build();

        // Mock the service call
        when(bookService.getBookById(1L)).thenReturn(bookDTO);

        // Perform the GET request and validate the response
        mockMvc.perform(get("/api/books/1")
                        .header("Authorization", "Bearer " + librarianToken)) // Pass the valid token
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("1984"))
                .andExpect(jsonPath("$.author").value("George Orwell"))
                .andExpect(jsonPath("$.isbn").value("9780451524935"));
        logger.info("Completed Test: shouldGetBookByID_whenRequestIsValid");
    }

    @Test
    void shouldFailToGetBookByID_whenAuthorizationTokenIsMissing() throws Exception {
        logger.info("Starting Test: shouldFailToGetBookByID_whenAuthorizationTokenIsMissing");
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isUnauthorized());
        logger.info("Completed Test: shouldFailToGetBookByID_whenAuthorizationTokenIsMissing");
    }

    @Test
    @WithMockUser(username = "Librarian", roles = "LIBRARIAN")
    void shouldFailToGetBookByID_whenBookIDNotFound() throws Exception {
        logger.info("Starting Test: shouldFailToGetBookByID_whenBookIDNotFound");
        Long nonExistentId = 999999L;
        when(bookService.getBookById(nonExistentId))
                .thenThrow(new BookNotFoundException("Book with ID " + nonExistentId + " not found"));

        mockMvc.perform(get("/api/books/" + nonExistentId)
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNotFound());
        logger.info("Completed Test: shouldFailToGetBookByID_whenBookIDNotFound");
    }


    @Test
    @WithMockUser(username = "Librarian", roles = "LIBRARIAN")
    void shouldUpdateBook_whenRequestIsValidAndUserIsLibrarian() throws Exception {
        logger.info("Starting Test: shouldUpdateBook_whenRequestIsValidAndUserIsLibrarian");
        // Setup the BookDTO for update
        BookDTO updatedBookDTO = BookDTO.builder()
                .title("Animal Farm")
                .author("George Orwell")
                .isbn("9780451526342")
                .publicationDate(LocalDate.of(1945, 8, 17))
                .genre("Dystopian")
                .available(true)
                .build();

        // Mock the service call
        when(bookService.updateBook(eq(1L), any(BookDTO.class))).thenReturn(updatedBookDTO);
        // Perform the PUT request and validate the response
        logger.debug("Created BookDTO for update: {}", updatedBookDTO);
        mockMvc.perform(put("/api/books/1")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBookDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Animal Farm"))
                .andExpect(jsonPath("$.author").value("George Orwell"))
                .andExpect(jsonPath("$.isbn").value("9780451526342"));
        logger.info("Completed Test: shouldUpdateBook_whenRequestIsValidAndUserIsLibrarian");
    }

    @Test
    void shouldFailToUpdateBook_whenUserIsNotLibrarian() throws Exception {
        logger.info("Starting Test: shouldFailToUpdateBook_whenUserIsNotLibrarian  ");
        Book book = bookRepository.save(Book.builder()
                .title("Test Book")
                .author("Author")
                .isbn("1111111111111")
                .publicationDate(LocalDate.of(2022, 1, 1))
                .genre("Drama")
                .available(true)
                .build());

        BookDTO updated = BookDTO.builder()
                .title("Unauthorized Update")
                .author("Bad Actor")
                .isbn("1111111111111")
                .publicationDate(LocalDate.of(2022, 1, 1))
                .genre("Drama")
                .available(true)
                .build();

        // Use Spring's configured ObjectMapper
        String jsonPayload = objectMapper.writeValueAsString(updated);

        mockMvc.perform(put("/api/books/" + book.getId())
                        .header("Authorization", "Bearer " + patronToken) // Not librarian
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isForbidden());
        logger.info("Completed Test: shouldFailToUpdateBook_whenUserIsNotLibrarian");

    }

    @Test
    void shouldFailToUpdateBook_whenAuthorizationTokenIsMissing() throws Exception {
        logger.info("Starting Test: shouldFailToUpdateBook_whenAuthorizationTokenIsMissing");

        Book book = bookRepository.save(Book.builder()
                .title("Private Book")
                .author("Secure Author")
                .isbn("9999999999999")
                .publicationDate(LocalDate.of(2021, 1, 1))
                .genre("Mystery")
                .available(true)
                .build());

        BookDTO update = BookDTO.builder()
                .title("Hacked Title")
                .author("Unknown")
                .isbn("9999999999999")
                .publicationDate(LocalDate.of(2021, 1, 1))
                .genre("Mystery")
                .available(false)
                .build();

        // Use Spring's configured ObjectMapper
        String jsonPayload = objectMapper.writeValueAsString(update);

        mockMvc.perform(put("/api/books/" + book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnauthorized());
        logger.info("Completed Test: shouldFailToUpdateBook_whenAuthorizationTokenIsMissing");

    }


    @Test
    @WithMockUser(username = "Librarian", roles = "LIBRARIAN")
    void shouldDeleteBook_whenRequestIsValidAndUserIsLibrarian() throws Exception {
        logger.info("Starting Test: shouldDeleteBook_whenRequestIsValidAndUserIsLibrarian");

        // Mock the service call
        doNothing().when(bookService).deleteBook(1L);

        // Perform the DELETE request and validate the response
        mockMvc.perform(delete("/api/books/1")
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNoContent());

        // Verify that deleteBook method was called
        verify(bookService, times(1)).deleteBook(1L);
        logger.info("Completed Test: shouldDeleteBook_whenRequestIsValidAndUserIsLibrarian");
    }


    @Test
    void shouldFailToDeleteBook_whenUserIsNotLibrarian() throws Exception {
        logger.info("Starting Test: shouldFailToDeleteBook_whenUserIsNotLibrarian");

        Book book = bookRepository.save(Book.builder()
                .title("Test Title")
                .author("Test Author")
                .isbn("9780451526342")
                .publicationDate(LocalDate.of(2020, 1, 1))
                .genre("Science Fiction")
                .available(true)
                .build());


        mockMvc.perform(delete("/api/books/" + book.getId())
                        .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isForbidden());
        logger.info("Completed Test: shouldFailToDeleteBook_whenUserIsNotLibrarian");
    }

    @Test
    void shouldFailToDeleteBook_whenAuthorizationTokenIsMissing() throws Exception {
        logger.info("Starting Test: shouldFailToDeleteBook_whenAuthorizationTokenIsMissing");
        Book book = bookRepository.save(Book.builder()
                .title("Test Title")
                .author("Test Author")
                .isbn("9780451526342")
                .publicationDate(LocalDate.of(2020, 1, 1))
                .genre("Science Fiction")
                .available(true)
                .build());

        mockMvc.perform(delete("/api/books/" + book.getId()))
                .andExpect(status().isUnauthorized());
        logger.info("Completed Test: shouldFailToDeleteBook_whenAuthorizationTokenIsMissing");
    }




    @Test
    @WithMockUser(username = "Librarian", roles = "LIBRARIAN")
    void shouldSearchBook_whenRequestIsValid() throws Exception {
        logger.info("Starting Test: shouldSearchBook_whenRequestIsValid");
        // Setup the search keyword
        String keyword = "George Orwell";

        mockMvc.perform(get("/api/books/search?keyword=" + keyword + "&page=0&size=10")
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());
        logger.info("Completed Test: shouldSearchBook_whenRequestIsValid");
    }

    @Test
    void shouldFailToSearchBook_whenAuthorizationTokenIsMissing() throws Exception {
        logger.info("Starting Test: shouldFailToSearchBook_whenAuthorizationTokenIsMissing");
        // Setup the search keyword
        String keyword = "George Orwell";
        // Perform the GET request with the search query and validate the response
        mockMvc.perform(get("/api/books/search?keyword=" + keyword + "&page=0&size=10"))
                .andExpect(status().isUnauthorized());
        logger.info("Completed Test: shouldFailToSearchBook_whenAuthorizationTokenIsMissing");
    }
}
