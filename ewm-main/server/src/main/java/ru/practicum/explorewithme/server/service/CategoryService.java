package ru.practicum.explorewithme.server.service;

import ru.practicum.explorewithme.server.dto.category.CategoryDto;
import ru.practicum.explorewithme.server.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long categoryId);

    CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategoryById(Long categoryId);
}
