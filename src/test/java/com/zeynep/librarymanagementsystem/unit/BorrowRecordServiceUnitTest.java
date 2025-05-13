package com.zeynep.librarymanagementsystem.unit;


import com.zeynep.librarymanagementsystem.dto.BorrowRecordDTO;
import com.zeynep.librarymanagementsystem.mapper.BorrowRecordMapper;
import com.zeynep.librarymanagementsystem.model.Book;
import com.zeynep.librarymanagementsystem.model.BorrowRecord;
import com.zeynep.librarymanagementsystem.model.User;
import com.zeynep.librarymanagementsystem.repository.BookRepository;
import com.zeynep.librarymanagementsystem.repository.BorrowRecordRepository;
import com.zeynep.librarymanagementsystem.repository.UserRepository;
import com.zeynep.librarymanagementsystem.exception.*;

import com.zeynep.librarymanagementsystem.service.iml.BorrowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BorrowRecordServiceUnitTest {
    private static final Logger logger = LoggerFactory.getLogger(BorrowRecordServiceUnitTest.class);

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowRecordMapper borrowRecordMapper;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    private User user;
    private Book book;
    private BorrowRecord borrowRecord;
    private BorrowRecordDTO borrowRecordDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder().id(1L).build();
        book = Book.builder().id(2L).available(true).build();

        borrowRecord = BorrowRecord.builder()
                .id(1L)
                .user(user)
                .book(book)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusWeeks(2))
                .build();

        borrowRecordDTO = BorrowRecordDTO.builder()
                .id(1L)
                .userId(1L)
                .bookId(2L)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusWeeks(2))
                .build();
    }

    @Test
    void shouldBorrowBook_whenValidUserAndBook() {
        logger.info("Starting test: shouldBorrowBook_whenValidUserAndBook");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(borrowRecord);
        when(borrowRecordMapper.toDTO(any(BorrowRecord.class))).thenReturn(borrowRecordDTO);

        BorrowRecordDTO result = borrowService.borrowBook(1L, 2L);

        logger.debug("Received BorrowRecordDTO: {}", result);
        assertEquals(1L, result.getUserId());
        assertEquals(2L, result.getBookId());
        verify(bookRepository).save(book);
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
        logger.info("Completed test: shouldBorrowBook_whenValidUserAndBook");
    }

    @Test
    void shouldThrowException_whenUserNotFound() {
        logger.info("Starting test: shouldThrowException_whenUserNotFound");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> borrowService.borrowBook(1L, 2L));
        logger.info("Completed test: shouldThrowException_whenBookNotFound - exception thrown as expected");
    }

    @Test
    void shouldThrowException_whenBookNotFound() {
        logger.info("Test started: shouldThrowException_whenBookNotFound");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> borrowService.borrowBook(1L, 2L));
        logger.info("Test completed: shouldThrowException_whenBookNotFound - exception thrown as expected");
    }

    @Test
    void shouldThrowException_whenBookNotAvailable() {
        logger.info("Test started: shouldThrowException_whenBookNotAvailable");
        book.setAvailable(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        assertThrows(BookNotAvailableException.class, () -> borrowService.borrowBook(1L, 2L));
        logger.info("Test completed: shouldThrowException_whenBookNotAvailable - exception thrown as expected");
    }

    @Test
    void shouldReturnBook_whenValidBorrowRecord() {
        logger.info("Test started: shouldReturnBook_whenValidBorrowRecord");

        borrowRecord.setReturnDate(null);
        when(borrowRecordRepository.findByUserIdAndBookIdAndReturnDateIsNull(1L, 2L))
                .thenReturn(Optional.of(borrowRecord));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(borrowRecord);
        when(borrowRecordMapper.toDTO(any(BorrowRecord.class))).thenReturn(borrowRecordDTO);

        BorrowRecordDTO result = borrowService.returnBook(1L, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        verify(bookRepository).save(book);

        logger.debug("Returned BorrowRecordDTO: {}", result);
        logger.info("Test completed: shouldReturnBook_whenValidBorrowRecord");
    }

    @Test
    void shouldThrowException_whenActiveBorrowRecordNotFound() {
        logger.info("Test started: shouldThrowException_whenActiveBorrowRecordNotFound");

        when(borrowRecordRepository.findByUserIdAndBookIdAndReturnDateIsNull(1L, 2L))
                .thenReturn(Optional.empty());
        assertThrows(ActiveBorrowRecordNotFoundException.class, () -> borrowService.returnBook(1L, 2L));
        logger.info("Test completed: shouldThrowException_whenActiveBorrowRecordNotFound - exception thrown as expected");
    }

    @Test
    void shouldGetUserBorrowHistory_whenUserHasBorrowedBooks() {
        logger.info("Test started: shouldGetUserBorrowHistory_whenUserHasBorrowedBooks");
        List<BorrowRecord> records = List.of(borrowRecord);
        when(borrowRecordRepository.findByUserId(1L)).thenReturn(records);
        when(borrowRecordMapper.toDTO(any(BorrowRecord.class))).thenReturn(borrowRecordDTO);

        List<BorrowRecordDTO> result = borrowService.getUserBorrowHistory(1L);

        assertEquals(1, result.size());
        verify(borrowRecordRepository).findByUserId(1L);

        logger.debug("User borrow history: {}", result);
        logger.info("Test completed: shouldGetUserBorrowHistory_whenUserHasBorrowedBooks");
    }

    @Test
    void shouldGetAllBorrowHistory_whenBorrowRecordsExist() {
        logger.info("Test started: shouldGetAllBorrowHistory_whenBorrowRecordsExist");
        when(borrowRecordRepository.findAll()).thenReturn(List.of(borrowRecord));
        when(borrowRecordMapper.toDTO(any(BorrowRecord.class))).thenReturn(borrowRecordDTO);

        List<BorrowRecordDTO> result = borrowService.getAllBorrowHistory();

        assertEquals(1, result.size());
        logger.debug("All borrow records: {}", result);
        logger.info("Test completed: shouldGetAllBorrowHistory_whenBorrowRecordsExist");

    }

    @Test
    void shouldGetOverdueBooks_whenBooksAreOverdue() {
        logger.info("Test started: shouldGetOverdueBooks_whenBooksAreOverdue");

        when(borrowRecordRepository.findByDueDateBeforeAndReturnDateIsNull(any(LocalDate.class)))
                .thenReturn(List.of(borrowRecord));
        when(borrowRecordMapper.toDTO(any(BorrowRecord.class))).thenReturn(borrowRecordDTO);

        List<BorrowRecordDTO> result = borrowService.getOverdueBooks();

        assertEquals(1, result.size());
        logger.debug("Overdue books: {}", result);
        logger.info("Test completed: shouldGetOverdueBooks_whenBooksAreOverdue");
    }
}
