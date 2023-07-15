package ru.practicum.category.service;

import ru.practicum.category.model.dto.CategoryDto;
import ru.practicum.category.model.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    List<CategoryDto> getAllCategory(Integer from, Integer size);

    CategoryDto find(long catId);

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(long catId, NewCategoryDto newCategoryDto);

    void deleteCategory(long catId);


}