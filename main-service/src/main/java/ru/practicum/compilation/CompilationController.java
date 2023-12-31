package ru.practicum.compilation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.model.dto.CompilationDto;
import ru.practicum.compilation.model.dto.FreshCompilationDto;
import ru.practicum.compilation.model.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
public class CompilationController {

    private final CompilationService compilationService;

    public CompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping("/compilations")
    public ResponseEntity<List<CompilationDto>> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                                @RequestParam(defaultValue = "0") Integer from,
                                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен GET запрос: /compilations endpoint");
        return ResponseEntity.ok().body(compilationService.getCompilations(pinned, from, size));
    }

    @GetMapping("/compilations/{compId}")
    public ResponseEntity<CompilationDto> findCompilation(@PathVariable Long compId) {
        log.info("Получен GET запрос: /compilations/{} endpoint", compId);
        return ResponseEntity.ok().body(compilationService.searchCompilation(compId));
    }

    @PostMapping("/admin/compilations")
    public ResponseEntity<CompilationDto> createCompilation(@RequestBody FreshCompilationDto freshCompilationDto) {
        log.info("Получен POST запрос: /admin/compilations endpoint with body={}", freshCompilationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(compilationService.createCompilation(freshCompilationDto));
    }

    @DeleteMapping("/admin/compilations/{compId}")
    public ResponseEntity<CompilationDto> deleteCompilation(@PathVariable Long compId) {
        log.info("Получен POST запрос: /admin/compilations/{} endpoint", compId);
        compilationService.deleteCompilation(compId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/compilations/{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(@PathVariable Long compId,
                                                            @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        log.info("Получен PATCH запрос: /admin/compilations/{} endpoint with body={}", compId, updateCompilationRequest);
        return ResponseEntity.ok().body(compilationService.updateCompilation(compId, updateCompilationRequest));
    }

}