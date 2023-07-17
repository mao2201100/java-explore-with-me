package ru.practicum.location.mapper;

import org.mapstruct.Mapper;
import ru.practicum.location.model.Location;
import ru.practicum.location.model.dto.LocationDto;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location toLocation(LocationDto locationDto);

    LocationDto toLocationDto(Location location);

}