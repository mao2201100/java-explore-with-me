package ru.practicum.hit.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HitDTO {

    private String app;
    private String uri;
    private String ip;
    private String timestamp;

}