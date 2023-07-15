package ru.practicum.compilation.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.event.model.dto.EventMinDto;

import java.util.List;

@Getter
@Setter
@Builder
public class CompilationDto {

    private Long id;
    private List<EventMinDto> events;
    private Boolean pinned;
    private String title;

}