package com.zeynep.librarymanagementsystem.mapper;


import com.zeynep.librarymanagementsystem.dto.BookDTO;
import com.zeynep.librarymanagementsystem.model.Book;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {


    BookDTO toDto(Book book);
    Book toEntity(BookDTO bookDTO);
    List<BookDTO> toDtoList(List<Book> books);
}