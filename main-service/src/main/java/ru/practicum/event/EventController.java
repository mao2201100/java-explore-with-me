package ru.practicum.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.ClientHit;
import ru.practicum.event.model.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.hit.dto.HitDTO;
import ru.practicum.request.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.model.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
public class EventController {

    private final EventService eventService;
    private final ClientHit hitClient;

    public EventController(EventService eventService, ClientHit hitClient) {
        this.eventService = eventService;
        this.hitClient = hitClient;
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventMinDto>> getAllEvents(@RequestParam(required = false) String text,
                                                          @RequestParam(required = false) List<Long> categories,
                                                          @RequestParam(required = false) Boolean paid,
                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                          @RequestParam(required = false) Boolean onlyAvailable,
                                                          @RequestParam(required = false) String sort,
                                                          @RequestParam(defaultValue = "0") Integer from,
                                                          @RequestParam(defaultValue = "10") Integer size,
                                                          HttpServletRequest request) {
        log.info("Получен GET запрос: /events endpoint");
        addStat(request);
        return ResponseEntity.ok().body(eventService.getAll(text, categories, paid, rangeStart,
                rangeEnd, onlyAvailable, sort, from, size));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventDto> findEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("Получен GET запрос: /events/{}", id);
        addStat(request);
        return ResponseEntity.ok().body(eventService.find(id));
    }

    private void addStat(HttpServletRequest request) {
        HitDTO endpointHit = HitDTO.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
        ResponseEntity<Object> response = hitClient.postHit(endpointHit);
        log.info("Добавлена статистика {}", response);
    }

    @GetMapping("/admin/events")
    public ResponseEntity<List<EventDto>> getAdminAllEvents(@RequestParam(required = false) List<Long> users,
                                                            @RequestParam(required = false) List<String> states,
                                                            @RequestParam(required = false) List<Long> categories,
                                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                            @RequestParam(defaultValue = "0") Integer from,
                                                            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен GET запрос: /admin/events endpoint with parameters " +
                        "users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        return ResponseEntity.ok().body(eventService.getAdminAllEvents(users,
                states, categories, rangeStart, rangeEnd, from, size));
    }

    @PatchMapping("/admin/events/{eventId}")
    public ResponseEntity<EventDto> updateAdminEvent(@PathVariable Long eventId,
                                                     @RequestBody ChangeEventAdminRequest updateRequest) {
        log.info("Получен PATCH запрос: /admin/events/{} c телом {}", eventId, updateRequest);
        return ResponseEntity.ok().body(eventService.updateAdminEvent(eventId, updateRequest));
    }

    @GetMapping("/users/{userId}/events")
    public ResponseEntity<List<EventMinDto>> getUserEvents(@PathVariable Long userId,
                                                           @RequestParam(defaultValue = "0") Integer from,
                                                           @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен GET запрос: /users/{}/events", userId);
        return ResponseEntity.ok().body(eventService.getUserEvents(userId, from, size));
    }

    @PostMapping("/users/{userId}/events")
    public ResponseEntity<EventDto> createUserEvent(@PathVariable Long userId,
                                                    @RequestBody(required = false) FreshEventDto freshEventDto) {
        log.info("Получен POST запрос: /users/{}/events с телом {}", userId, freshEventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createUserEvent(userId, freshEventDto));
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public ResponseEntity<EventDto> findUserEvent(@PathVariable Long userId,
                                                  @PathVariable Long eventId) {
        log.info("Получен GET запрос: /users/{}/events/{} ", userId, eventId);
        return ResponseEntity.ok().body(eventService.findUserEvent(userId, eventId));
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public ResponseEntity<EventDto> updateUserEvent(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @RequestBody(required = false) UpdateEventUserRequest updateEventUserRequest) {
        log.info("Получен PATCH запрос: /users/{}/events/{} ", userId, eventId);
        return ResponseEntity.ok().body(eventService.updateUserEvent(userId, eventId, updateEventUserRequest));
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> findUserEventRequests(@PathVariable Long userId,
                                                                               @PathVariable Long eventId) {
        log.info("Получен GET запрос: /users/{}/events/{}/requests ", userId, eventId);
        return ResponseEntity.ok().body(eventService.findUserEventRequests(userId, eventId));
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateUserEventRequests(@PathVariable Long userId,
                                                                                  @PathVariable Long eventId,
                                                                                  @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("Получен PATCH запрос: /users/{}/events/{}/requests с телом {}", userId, eventId, updateRequest);
        return ResponseEntity.ok().body(eventService.updateUserEventRequests(userId, eventId, updateRequest));
    }

}