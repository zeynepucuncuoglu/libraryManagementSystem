package com.zeynep.librarymanagementsystem.mapper;

import com.zeynep.librarymanagementsystem.dto.BorrowRecordDTO;
import com.zeynep.librarymanagementsystem.model.BorrowRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BorrowRecordMapper {

    BorrowRecordMapper INSTANCE = Mappers.getMapper(BorrowRecordMapper.class);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "book.id", target = "bookId")
    BorrowRecordDTO toDTO(BorrowRecord borrowRecord);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "bookId", target = "book.id")
    BorrowRecord toEntity(BorrowRecordDTO borrowRecordDTO);
}
