package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService){
        this.requestService =requestService;
    }

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable Long userId) {
        log.info("Получен GET запрос: /users/{}/", userId);
        return ResponseEntity.ok().body(requestService.getUserRequests(userId));
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> createUserRequest(@PathVariable Long userId,
                                                                     @RequestParam(required = false) Long eventId) {
        log.info("Получен POST запрос: /users/{}/requests?eventId={} ", userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.createUserRequest(userId, eventId));
    }

    @PatchMapping("{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelUserRequest(@PathVariable Long userId,
                                                                     @PathVariable Long requestId) {
        log.info("Получен PATCH запрос: /users/{}/requests/{}/cancel ", userId, requestId);
        return ResponseEntity.ok().body(requestService.cancelUserRequest(userId, requestId));
    }

}