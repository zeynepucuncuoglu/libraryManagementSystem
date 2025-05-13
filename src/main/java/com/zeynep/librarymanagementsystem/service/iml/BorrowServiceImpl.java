package com.zeynep.librarymanagementsystem.service.iml;

import com.zeynep.librarymanagementsystem.dto.BorrowRecordDTO;
import com.zeynep.librarymanagementsystem.exception.ActiveBorrowRecordNotFoundException;
import com.zeynep.librarymanagementsystem.exception.BookNotAvailableException;
import com.zeynep.librarymanagementsystem.exception.BookNotFoundException;
import com.zeynep.librarymanagementsystem.exception.UserNotFoundException;
import com.zeynep.librarymanagementsystem.mapper.BorrowRecordMapper;
import com.zeynep.librarymanagementsystem.model.Book;
import com.zeynep.librarymanagementsystem.model.BorrowRecord;
import com.zeynep.librarymanagementsystem.model.User;
import com.zeynep.librarymanagementsystem.repository.BookRepository;
import com.zeynep.librarymanagementsystem.repository.BorrowRecordRepository;
import com.zeynep.librarymanagementsystem.repository.UserRepository;
import com.zeynep.librarymanagementsystem.service.BorrowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class BorrowServiceImpl implements BorrowService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowServiceImpl.class);


    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRecordMapper borrowRecordMapper;

    @Autowired
    public BorrowServiceImpl(BorrowRecordRepository borrowRecordRepository, BookRepository bookRepository,
                             UserRepository userRepository, BorrowRecordMapper borrowRecordMapper) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.borrowRecordMapper = borrowRecordMapper;
    }

    @Override
    public BorrowRecordDTO borrowBook(Long userId, Long bookId) {
        logger.info("Attempting to borrow book with ID {} for user with ID {}", bookId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User with ID {} not found", userId);
                    return new UserNotFoundException("User not found");
                });

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    logger.warn("Book with ID {} not found", bookId);
                    return new BookNotFoundException("Book not found");
                });

        if (!book.isAvailable()) {
            logger.warn("Book with ID {} is not available for borrowing", bookId);
            throw new BookNotAvailableException("Book is not available for borrowing");
        }

        BorrowRecord borrowRecord = BorrowRecord.builder()
                .user(user)
                .book(book)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusWeeks(2))
                .build();

        book.setAvailable(false);
        bookRepository.save(book);
        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);

        logger.info("Book with ID {} successfully borrowed by user with ID {}", bookId, userId);
        return borrowRecordMapper.toDTO(savedRecord);
    }

    @Override
    public BorrowRecordDTO returnBook(Long userId, Long bookId) {
        logger.info("Attempting to return book with ID {} for user with ID {}", bookId, userId);

        BorrowRecord borrowRecord = borrowRecordRepository.findByUserIdAndBookIdAndReturnDateIsNull(userId, bookId)
                .orElseThrow(() -> {
                    logger.warn("No active borrow record found for user ID {} and book ID {}", userId, bookId);
                    return new ActiveBorrowRecordNotFoundException("No active borrow record found for this user and book");
                });

        borrowRecord.setReturnDate(LocalDate.now());

        Book book = borrowRecord.getBook();
        book.setAvailable(true);
        bookRepository.save(book);

        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
        logger.info("Book with ID {} successfully returned by user with ID {}", bookId, userId);
        return borrowRecordMapper.toDTO(savedRecord);
    }

    @Override
    public List<BorrowRecordDTO> getUserBorrowHistory(Long userId) {
        logger.info("Fetching borrow history for user with ID {}", userId);
        return borrowRecordRepository.findByUserId(userId).stream()
                .map(borrowRecordMapper::toDTO)
                .toList();
    }

    @Override
    public List<BorrowRecordDTO> getAllBorrowHistory() {
        logger.info("Fetching complete borrow history");
        return borrowRecordRepository.findAll().stream()
                .map(borrowRecordMapper::toDTO)
                .toList();
    }

    @Override
    public List<BorrowRecordDTO> getOverdueBooks() {
        logger.info("Fetching overdue books");
        return borrowRecordRepository.findByDueDateBeforeAndReturnDateIsNull(LocalDate.now()).stream()
                .map(borrowRecordMapper::toDTO)
                .toList();
    }
}
