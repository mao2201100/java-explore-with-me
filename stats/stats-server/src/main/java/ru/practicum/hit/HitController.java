package ru.practicum.hit;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import ru.practicum.hit.dto.HitDTO;
import ru.practicum.hit.service.HitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statisticsDto.ViewStatsDTO;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HitController {

    private final HitService hitService;

    @PostMapping(value = "/hit")
    public ResponseEntity<HitDTO> addNewHit(@RequestBody HitDTO endpointHitDTO) {
        log.info("Received POST addNewHit");
        return ResponseEntity.status(HttpStatus.CREATED).body(hitService.createNewHit(endpointHitDTO));
    }

    @GetMapping(value = "/stats")
    public ResponseEntity<List<ViewStatsDTO>> getStats(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                                       @RequestParam(required = false) List<String> uris,
                                                       @RequestParam(defaultValue = "false") String unique) {
        log.info("Received GET getStats");
        return ResponseEntity.ok().body(hitService.readAllStats(start, end, uris, unique));
    }

}