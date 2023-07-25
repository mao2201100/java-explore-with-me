package ru.practicum.rating.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.user.model.dto.UserShortDto;

import java.util.List;

@Getter
@Setter
@Builder
public class UserTopRating {

    private List<UserShortDto> users;

}