package ru.practicum.rating.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.event.model.dto.EventMinDto;

@Getter
@Setter
@Builder
public class EventRating {

    private EventMinDto event;
    private String rating;

}