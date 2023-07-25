package ru.practicum.rating.service;

import ru.practicum.rating.dto.EventRating;
import ru.practicum.rating.dto.EventTopRating;
import ru.practicum.rating.dto.EventsRating;
import ru.practicum.rating.dto.UserTopRating;

import java.util.List;

public interface RatingService {

    EventsRating createEventsLike(Long userId, List<Long> eventIds);

    EventRating createEventLike(Long userId, Long eventId);

    EventsRating createEventsDislike(Long userId, List<Long> eventIds);

    EventRating createEventDislike(Long userId, Long eventId);

    void deleteEventsLike(Long userId, List<Long> eventIds);

    void deleteEventLike(Long userId, Long eventId);

    void deleteEventsDislike(Long userId, List<Long> eventIds);

    void deleteEventDislike(Long userId, Long eventId);

    EventTopRating getEventRating(Integer top);

    UserTopRating getUserRating(Integer top);

}