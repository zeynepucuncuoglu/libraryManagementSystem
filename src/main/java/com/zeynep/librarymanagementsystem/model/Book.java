package com.zeynep.librarymanagementsystem.model;



import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @Column(unique = true)
    @NotBlank
    private String isbn;

    @PastOrPresent
    private LocalDate publicationDate;

    @NotBlank
    private String genre;

    @Column(nullable = false)
    private boolean available = true;

    // Optional: List of borrow records
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private java.util.List<BorrowRecord> borrowRecords;
}
