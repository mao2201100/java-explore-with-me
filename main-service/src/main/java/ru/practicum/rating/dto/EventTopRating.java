package ru.practicum.rating.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.event.model.dto.EventMinDto;

import java.util.List;

@Getter
@Setter
@Builder
public class EventTopRating {

    private List<EventMinDto> events;

}