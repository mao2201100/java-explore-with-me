package ru.practicum.user.service;

import ru.practicum.user.model.dto.NewUserRequest;
import ru.practicum.user.model.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    UserDto createUser(NewUserRequest newUserRequest);

    void deleteUser(long userId);

}