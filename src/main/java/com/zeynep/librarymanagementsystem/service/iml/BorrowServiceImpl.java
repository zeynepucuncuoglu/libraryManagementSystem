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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class BorrowServiceImpl implements BorrowService {

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found"));

        if (!book.isAvailable()) {
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
        return borrowRecordMapper.toDTO(borrowRecordRepository.save(borrowRecord));
    }

    @Override
    public BorrowRecordDTO returnBook(Long userId, Long bookId) {
        BorrowRecord borrowRecord = borrowRecordRepository.findByUserIdAndBookIdAndReturnDateIsNull(userId, bookId)
                .orElseThrow(() -> new ActiveBorrowRecordNotFoundException("No active borrow record found for this user and book"));

        borrowRecord.setReturnDate(LocalDate.now());

        Book book = borrowRecord.getBook();
        book.setAvailable(true);
        bookRepository.save(book);

        return borrowRecordMapper.toDTO(borrowRecordRepository.save(borrowRecord));
    }

    @Override
    public List<BorrowRecordDTO> getUserBorrowHistory(Long userId) {
        return borrowRecordRepository.findByUserId(userId).stream()
                .map(borrowRecordMapper::toDTO)
                .toList();
    }

    @Override
    public List<BorrowRecordDTO> getAllBorrowHistory() {
        return borrowRecordRepository.findAll().stream()
                .map(borrowRecordMapper::toDTO)
                .toList();
    }

    @Override
    public List<BorrowRecordDTO> getOverdueBooks() {
        return borrowRecordRepository.findByDueDateBeforeAndReturnDateIsNull(LocalDate.now()).stream()
                .map(borrowRecordMapper::toDTO)
                .toList();
    }
}
