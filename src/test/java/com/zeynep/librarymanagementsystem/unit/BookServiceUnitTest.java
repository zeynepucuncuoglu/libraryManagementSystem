package com.zeynep.librarymanagementsystem.unit;


import com.zeynep.librarymanagementsystem.dto.BookDTO;
import com.zeynep.librarymanagementsystem.exception.BookNotFoundException;
import com.zeynep.librarymanagementsystem.exception.ISBNAlreadyExistsException;
import com.zeynep.librarymanagementsystem.mapper.BookMapper;
import com.zeynep.librarymanagementsystem.model.Book;
import com.zeynep.librarymanagementsystem.repository.BookRepository;
import com.zeynep.librarymanagementsystem.service.iml.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceUnitTest {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceUnitTest.class);

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        book = Book.builder()
                .id(1L)
                .title("Test Title")
                .author("Test Author")
                .isbn("9781234567890")
                .publicationDate(LocalDate.now())
                .genre("Fiction")
                .available(true)
                .build();

        bookDTO = BookDTO.builder()
                .id(1L)
                .title("Test Title")
                .author("Test Author")
                .isbn("9781234567890")
                .publicationDate(LocalDate.now())
                .genre("Fiction")
                .available(true)
                .build();
    }

    @Test
    void shouldAddBook_whenValidData() {
        logger.info("Starting test: shouldAddBook_whenValidData");
        when(bookRepository.existsByIsbn(bookDTO.getIsbn())).thenReturn(false);
        when(bookMapper.toEntity(bookDTO)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDTO);

        BookDTO result = bookService.addBook(bookDTO);

        logger.debug("Added Book result: {}", result);
        assertThat(result).isEqualTo(bookDTO);
        verify(bookRepository).save(book);
        logger.info("Completed test: shouldAddBook_whenValidData successfully");
    }

    @Test
    void shouldThrowException_whenDuplicateISBN() {
        logger.info("Starting test: shouldThrowException_whenDuplicateISBN");
        when(bookRepository.existsByIsbn(bookDTO.getIsbn())).thenReturn(true);

        assertThatThrownBy(() -> bookService.addBook(bookDTO))
                .isInstanceOf(ISBNAlreadyExistsException.class);
        logger.info("Completed test: shouldThrowException_whenDuplicateISBN - ISBNAlreadyExistsException thrown");
    }

    @Test
    void shouldGetBookById_whenBookExists() {
        logger.info("Starting test: shouldGetBookById_whenBookExists");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookDTO);

        BookDTO result = bookService.getBookById(1L);

        assertThat(result).isEqualTo(bookDTO);
        logger.info("Completed test: shouldGetBookById_whenBookExists successfully");
    }

    @Test
    void shouldThrowException_whenBookNotFound() {
        logger.info("Starting test: shouldThrowException_whenBookNotFound");
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(1L))
                .isInstanceOf(BookNotFoundException.class);
        logger.info("Completed test: shouldThrowException_whenBookNotFound - BookNotFoundException thrown");
    }

    @Test
    void shouldUpdateBook_whenValidData() {
        logger.info("Starting test: shouldUpdateBook_whenValidData");
        BookDTO updatedDTO = BookDTO.builder()
                .title("New Title")
                .author("New Author")
                .isbn("9789876543210")
                .publicationDate(LocalDate.now())
                .genre("Drama")
                .available(false)
                .build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toDto(any(Book.class))).thenReturn(updatedDTO);

        BookDTO result = bookService.updateBook(1L, updatedDTO);

        logger.debug("Updated Book result: {}", result);
        assertThat(result.getTitle()).isEqualTo("New Title");
        verify(bookRepository).save(book);
        logger.info("Completed test: shouldUpdateBook_whenValidData successfully");
    }

    @Test
    void shouldDeleteBook_whenBookExists() {
        logger.info("Starting test: shouldDeleteBook_whenBookExists");
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.deleteBook(1L);

        verify(bookRepository).deleteById(1L);
        logger.info("Completed test: shouldDeleteBook_whenBookExists successfully");
    }

    @Test
    void shouldThrowException_whenBookNotFoundForDeletion() {
        logger.info("Starting test: shouldThrowException_whenBookNotFoundForDeletion");
        when(bookRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(1L))
                .isInstanceOf(BookNotFoundException.class);
        logger.info("Completed test: shouldThrowException_whenBookNotFoundForDeletion - BookNotFoundException thrown");
    }

    @Test
    void shouldGetAllBooks_whenBooksExist() {
        logger.info("Starting test: shouldGetAllBooks_whenBooksExist");
        List<Book> books = List.of(book);
        List<BookDTO> bookDTOs = List.of(bookDTO);

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toDtoList(books)).thenReturn(bookDTOs);

        List<BookDTO> result = bookService.getAllBooks();
        logger.debug("Get all Books result: {}", result);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(bookDTO);
        logger.info("Completed test: shouldGetAllBooks_whenBooksExist successfully");
    }

    @Test
    void shouldSearchBooks_whenKeywordProvided() {
        logger.info("Starting test: shouldSearchBooks_whenKeywordProvided");
        Page<Book> bookPage = new PageImpl<>(List.of(book));
        Page<BookDTO> bookDTOPage = new PageImpl<>(List.of(bookDTO));

        when(bookRepository.searchByKeyword(eq("Test"), any())).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDTO);

        Page<BookDTO> result = bookService.searchBooks("Test", 0, 10);

        assertThat(result.getContent()).hasSize(1);

        logger.info("Completed test: shouldSearchBooks_whenKeywordProvided successfully");
    }

    @Test
    void shouldSearchBooks_whenNoKeywordProvided() {
        logger.info("Starting test: shouldSearchBooks_whenNoKeywordProvided");
        Page<Book> bookPage = new PageImpl<>(List.of(book));
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDTO);

        Page<BookDTO> result = bookService.searchBooks("", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        logger.info("Completed test: shouldSearchBooks_whenNoKeywordProvided successfully");
    }
}
