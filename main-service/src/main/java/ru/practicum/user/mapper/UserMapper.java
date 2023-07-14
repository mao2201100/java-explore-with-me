package ru.practicum.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.user.model.User;
import ru.practicum.user.model.dto.NewUserRequest;
import ru.practicum.user.model.dto.UserDto;
import ru.practicum.user.model.dto.UserShortDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto toUserDto(User user);

    User toUser(UserDto userDto);

    UserShortDto toUserShortDto(User user);

    User toUser(UserShortDto userShortDto);

    NewUserRequest toNewUserRequest(User user);

    User toUser(NewUserRequest newUserRequest);

}