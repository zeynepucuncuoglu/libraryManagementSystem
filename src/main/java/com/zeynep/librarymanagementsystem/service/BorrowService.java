package com.zeynep.librarymanagementsystem.service;

import com.zeynep.librarymanagementsystem.dto.BorrowRecordDTO;
import com.zeynep.librarymanagementsystem.model.BorrowRecord;
import java.util.List;

public interface BorrowService {

    BorrowRecordDTO borrowBook(Long userId, Long bookId);

    BorrowRecordDTO returnBook(Long userId, Long bookId);

    List<BorrowRecordDTO> getUserBorrowHistory(Long userId);

    List<BorrowRecordDTO> getAllBorrowHistory();

    List<BorrowRecordDTO> getOverdueBooks();
}
