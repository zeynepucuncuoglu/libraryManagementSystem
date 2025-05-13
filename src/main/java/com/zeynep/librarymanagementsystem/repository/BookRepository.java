package com.zeynep.librarymanagementsystem.repository;

import com.zeynep.librarymanagementsystem.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);


    @Query("""
        SELECT b FROM Book b
        WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);


}