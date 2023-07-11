package ru.practicum.request.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.request.model.RequestStatus;

import java.util.List;

@Getter
@Setter
@Builder
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds;
    private RequestStatus status;

}