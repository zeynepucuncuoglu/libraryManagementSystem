package com.zeynep.librarymanagementsystem.service;
import com.zeynep.librarymanagementsystem.dto.BookDTO;
import com.zeynep.librarymanagementsystem.model.Book;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;


public interface BookService {

    BookDTO addBook(BookDTO bookDTO);

    BookDTO getBookById(Long id);

    Page<BookDTO> searchBooks(String keyword, int page, int size);

    BookDTO updateBook(Long id, BookDTO updatedBookDTO);

    void deleteBook(Long id);

    List<BookDTO> getAllBooks();
}
