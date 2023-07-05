package ru.practicum.hit.mapper;

import org.mapstruct.Mapping;
import ru.practicum.hit.dto.HitDTO;
import ru.practicum.hit.model.Hit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HitMapper {

    @Mapping(target = "timestamp", source = "timestamp", dateFormat = "yyyy-MM-dd HH:mm:ss")
    HitDTO toEndpointHitDto(Hit endpointHit);

    @Mapping(target = "timestamp", source = "timestamp", dateFormat = "yyyy-MM-dd HH:mm:ss")
    Hit toEndpointHit(HitDTO endpointHitDTO);

}