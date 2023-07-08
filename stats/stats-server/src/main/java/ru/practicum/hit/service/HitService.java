package ru.practicum.hit.service;

import ru.practicum.hit.dto.HitDTO;
import ru.practicum.statisticsDto.ViewStatsDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface HitService {

    HitDTO createNewHit(HitDTO endpointHitDTO);

    List<ViewStatsDTO> readAllStats(LocalDateTime start, LocalDateTime end, List<String> uris, String unique);

}