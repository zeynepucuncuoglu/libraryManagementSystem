package com.zeynep.librarymanagementsystem.service.iml;

import com.zeynep.librarymanagementsystem.dto.BookDTO;

import com.zeynep.librarymanagementsystem.exception.BookNotFoundException;
import com.zeynep.librarymanagementsystem.exception.ISBNAlreadyExistsException;
import com.zeynep.librarymanagementsystem.mapper.BookMapper;
import com.zeynep.librarymanagementsystem.model.Book;
import com.zeynep.librarymanagementsystem.repository.BookRepository;
import com.zeynep.librarymanagementsystem.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;



@Service
public class BookServiceImpl implements BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    public BookDTO addBook(BookDTO bookDTO) {
        logger.info("Attempting to add book with ISBN: {}", bookDTO.getIsbn());
        if (bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            logger.error("Book with ISBN {} already exists", bookDTO.getIsbn());
            throw new ISBNAlreadyExistsException("A book with the ISBN " + bookDTO.getIsbn() + " already exists.");
        }
        Book book = bookMapper.toEntity(bookDTO);
        Book savedBook = bookRepository.save(book);
        logger.info("Successfully added book with ISBN: {}", savedBook.getIsbn());

        return bookMapper.toDto(savedBook);
    }

    @Override
    public BookDTO getBookById(Long id) {
        logger.info("Attempting to retrieve book with ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Book with ID {} not found", id);
                    return new BookNotFoundException("Book with ID " + id + " not found");
                });

        logger.info("Successfully retrieved book with ID: {}", id);
        return bookMapper.toDto(book);
    }

    @Override
    public Page<BookDTO> searchBooks(String keyword, int page, int size) {
        logger.info("Searching for books with keyword: {} on page {} with size {}", keyword, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> booksPage = bookRepository.searchByKeyword(keyword, pageable);

        if (keyword == null || keyword.trim().isEmpty()) {
            logger.info("No keyword provided, returning all books");
            return bookRepository.findAll(pageable).map(bookMapper::toDto);
        }

        logger.info("Found {} books for keyword: {}", booksPage.getTotalElements(), keyword);
        return booksPage.map(bookMapper::toDto);
    }


    @Override
    public BookDTO updateBook(Long id, BookDTO updatedBookDTO) {
        logger.info("Attempting to update book with ID: {}", id);

        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Book with ID {} not found for update", id);
                    return new BookNotFoundException("Book with ID " + id + " not found");
                });

        // Log details of the book being updated
        logger.info("Updating book with ID: {}. New details: Title: {}, Author: {}, ISBN: {}, Genre: {}, Available: {}",
                id, updatedBookDTO.getTitle(), updatedBookDTO.getAuthor(), updatedBookDTO.getIsbn(), updatedBookDTO.getGenre(), updatedBookDTO.isAvailable());

        // Update fields
        existingBook.setTitle(updatedBookDTO.getTitle());
        existingBook.setAuthor(updatedBookDTO.getAuthor());
        existingBook.setIsbn(updatedBookDTO.getIsbn());
        existingBook.setPublicationDate(updatedBookDTO.getPublicationDate());
        existingBook.setGenre(updatedBookDTO.getGenre());
        existingBook.setAvailable(updatedBookDTO.isAvailable());

        Book updatedBook = bookRepository.save(existingBook);

        logger.info("Successfully updated book with ID: {}", id);

        return bookMapper.toDto(updatedBook);
    }


    @Override
    public void deleteBook(Long id) {
        logger.info("Attempting to delete book with ID: {}", id);

        if (!bookRepository.existsById(id)) {
            logger.error("Book with ID {} not found for deletion", id);
            throw new BookNotFoundException("Book with ID " + id + " not found");
        }

        bookRepository.deleteById(id);
        logger.info("Successfully deleted book with ID: {}", id);
    }

    @Override
    public List<BookDTO> getAllBooks() {
        logger.info("Attempting to retrieve all books");

        List<BookDTO> books = bookMapper.toDtoList(bookRepository.findAll());

        logger.info("Successfully retrieved {} books", books.size());
        return books;
    }
}
