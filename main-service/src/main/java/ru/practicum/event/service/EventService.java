package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventDto createUserEvent(long userId, FreshEventDto freshEventDto);

    EventDto findUserEvent(long userId, long eventId);

    EventDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest updateRequest);

    List<EventMinDto> getAll(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                             LocalDateTime rangeEnd, Boolean onlyAvailable, String sort,
                             Integer from, Integer size);

    EventDto find(long id);

    List<EventDto> getAdminAllEvents(List<Long> users, List<String> states, List<Long> categories,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                     int from, int size);

    EventDto updateAdminEvent(long eventId, ChangeEventAdminRequest updateRequest);

    List<EventMinDto> getUserEvents(long userId, int from, int size);

    List<ParticipationRequestDto> findUserEventRequests(long userId, long eventId);

    EventRequestStatusUpdateResult updateUserEventRequests(long userId, long eventId,
                                                           EventRequestStatusUpdateRequest updateRequest);

}