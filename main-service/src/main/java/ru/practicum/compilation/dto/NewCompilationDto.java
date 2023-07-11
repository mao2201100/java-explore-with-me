package ru.practicum.compilation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class NewCompilationDto {

    private List<Long> events;
    private Boolean pinned;
    private String title;

}