package com.zeynep.librarymanagementsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;



@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // e.g., "LIBRARIAN" or "PATRON"

    @NotBlank
    private String contactInfo;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BorrowRecord> borrowRecords;
}
