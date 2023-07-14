package ru.practicum.compilation.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UpdateCompilationRequest {

    private List<Long> events;
    private Boolean pinned;
    private String title;

}