package com.zeynep.librarymanagementsystem.service.iml;

import com.zeynep.librarymanagementsystem.dto.BookDTO;

import com.zeynep.librarymanagementsystem.exception.BookNotFoundException;
import com.zeynep.librarymanagementsystem.exception.ISBNAlreadyExistsException;
import com.zeynep.librarymanagementsystem.mapper.BookMapper;
import com.zeynep.librarymanagementsystem.model.Book;
import com.zeynep.librarymanagementsystem.repository.BookRepository;
import com.zeynep.librarymanagementsystem.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;



@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    public BookDTO addBook(BookDTO bookDTO) {
        if (bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new ISBNAlreadyExistsException("A book with the ISBN " + bookDTO.getIsbn() + " already exists.");
        }
        Book book = bookMapper.toEntity(bookDTO);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found"));
        return bookMapper.toDto(book);
    }

    @Override
    public Page<BookDTO> searchBooks(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> booksPage = bookRepository.searchByKeyword(keyword, pageable);
        if (keyword == null || keyword.trim().isEmpty()) {
            return bookRepository.findAll(pageable).map(bookMapper::toDto);
        }
        return booksPage.map(bookMapper::toDto);
    }


    @Override
    public BookDTO updateBook(Long id, BookDTO updatedBookDTO) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found"));


        // Update fields
        existingBook.setTitle(updatedBookDTO.getTitle());
        existingBook.setAuthor(updatedBookDTO.getAuthor());
        existingBook.setIsbn(updatedBookDTO.getIsbn());
        existingBook.setPublicationDate(updatedBookDTO.getPublicationDate());
        existingBook.setGenre(updatedBookDTO.getGenre());
        existingBook.setAvailable(updatedBookDTO.isAvailable());

        return bookMapper.toDto(bookRepository.save(existingBook));
    }


    @Override
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book with ID " + id + " not found");
        }
        bookRepository.deleteById(id);
    }

    @Override
    public List<BookDTO> getAllBooks() {
        return bookMapper.toDtoList(bookRepository.findAll());
    }
}
