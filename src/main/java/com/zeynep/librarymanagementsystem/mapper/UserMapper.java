package com.zeynep.librarymanagementsystem.mapper;

import com.zeynep.librarymanagementsystem.dto.UserDTO;
import com.zeynep.librarymanagementsystem.model.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entity to DTO
    UserDTO toDTO(User user);

    // DTO to Entity
    User toEntity(UserDTO userDTO);
}
