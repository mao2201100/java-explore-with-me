package ru.practicum.compilation.service;

import ru.practicum.compilation.model.dto.CompilationDto;
import ru.practicum.compilation.model.dto.FreshCompilationDto;
import ru.practicum.compilation.model.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto searchCompilation(long compId);

    CompilationDto createCompilation(FreshCompilationDto freshCompilationDto);

    void deleteCompilation(long compId);

    CompilationDto updateCompilation(long compId, UpdateCompilationRequest updateCompilationRequest);

}