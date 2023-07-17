package ru.practicum.event.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.category.model.dto.CategoryDto;
import ru.practicum.event.model.EventStatatus;
import ru.practicum.location.model.dto.LocationDto;
import ru.practicum.user.model.dto.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EventDto {

    private Long id;
    private String annotation;
    private CategoryDto category;
    private int confirmedRequests;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private UserShortDto initiator;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private Boolean requestModeration;
    private EventStatatus state;
    private String title;
    private int views;

}