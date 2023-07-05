package ru.practicum.statisticsDto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ViewStatsDTO {

    private String app;
    private String uri;
    private Integer hits;

}