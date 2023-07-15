package ru.practicum.category;

import lombok.extern.slf4j.Slf4j;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.model.dto.CategoryDto;
import ru.practicum.category.model.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;


import java.util.List;

@Slf4j
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories") // Получение категории
    public ResponseEntity<List<CategoryDto>> getAll(@RequestParam(defaultValue = "0") Integer from,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен GET запрос: /categories?from={}&size={} endpoint", from, size);
        return ResponseEntity.ok().body(categoryService.getAllCategory(from, size));
    }

    @GetMapping("/categories/{id}") // Получение информации о категории по ее идентификатору
    public ResponseEntity<CategoryDto> findCategory(@PathVariable("id") Long catId) {
        log.info("Получен GET запрос: /categories/{} endpoint", catId);
        return ResponseEntity.ok().body(categoryService.find(catId));
    }

    @PostMapping("/admin/categories") //Добавление новой категории с уникальным именем
    public ResponseEntity<CategoryDto> createCategory(@RequestBody(required = false) NewCategoryDto newCategoryDto) {
        log.info("Получен POST запрос: /admin/categories endpoint with body={}", newCategoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(newCategoryDto));
    }

    @PatchMapping("/admin/categories/{id}") // Изменение категории
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable("id") Long catId,
                                                      @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Получен PATCH запрос: /admin/categories/{} endpoint with body {}", catId, newCategoryDto);
        return ResponseEntity.ok().body(categoryService.updateCategory(catId, newCategoryDto));
    }

    @DeleteMapping("/admin/categories/{id}") // Удаление категории
    public ResponseEntity<CategoryDto> deleteCategory(@PathVariable("id") Long catId) {
        log.info("Получен DELETE запрос: /admin/categories/{} endpoint", catId);
        categoryService.deleteCategory(catId);
        return ResponseEntity.noContent().build();
    }

}