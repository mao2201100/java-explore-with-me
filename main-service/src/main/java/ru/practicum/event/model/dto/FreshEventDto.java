package ru.practicum.event.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.location.model.dto.LocationDto;

@Getter
@Setter
@Builder
public class FreshEventDto {

    private String annotation;
    private Long category;
    private String description;
    private String eventDate;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String title;

}