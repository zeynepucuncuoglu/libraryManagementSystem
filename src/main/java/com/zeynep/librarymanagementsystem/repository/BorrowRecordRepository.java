package com.zeynep.librarymanagementsystem.repository;

import com.zeynep.librarymanagementsystem.model.BorrowRecord;
import com.zeynep.librarymanagementsystem.model.User;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByUserId(Long userId);

    List<BorrowRecord> findByDueDateBeforeAndReturnDateIsNull(LocalDate date);

    Optional<BorrowRecord> findByUserIdAndBookIdAndReturnDateIsNull(Long userId, Long bookId);
}