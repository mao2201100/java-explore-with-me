package ru.practicum.location.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LocationDto {

    private float lat;
    private float lon;

}