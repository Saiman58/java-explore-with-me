package ru.practicum.explorewithme.server.service;

import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.category.dto.NewCategoryDto;
import ru.practicum.explorewithme.server.entity.Category;

import java.util.List;

public interface CategoryService {

    // Создать категорию
    CategoryDto create(NewCategoryDto newCategory);

    // Обновить категорию
    CategoryDto update(Long catId, CategoryDto categoryDto);

    // Удалить категорию
    void delete(Long catId);

    // Получить все категории (с пагинацией)
    List<CategoryDto> getAll(Integer from, Integer size);

    // Получить категорию по ID
    CategoryDto getById(Long catId);

    // Получить entity категории по ID
    Category getEntityById(Long catId);
}