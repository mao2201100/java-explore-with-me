package ru.practicum.user.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NewUserRequest {

    private String name;
    private String email;

}