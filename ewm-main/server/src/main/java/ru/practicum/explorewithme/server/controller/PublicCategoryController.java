package ru.practicum.explorewithme.server.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.server.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Validated
public class PublicCategoryController {
    private final CategoryService categoryService;

    // GET /categories
    // Получение категорий с пагинацией
    @GetMapping
    public List<CategoryDto> getAll(
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("[PublicCategoryController] GET /categories?from={}&size={}", from, size);

        List<CategoryDto> categories = categoryService.getAll(from, size);
        log.debug("[PublicCategoryController] Найдено {} категорий", categories.size());

        return categories;
    }

    // GET /categories/{catId}
    // Получение информации о категории по её идентификатору
    @GetMapping("/{catId}")
    public CategoryDto getById(@PathVariable @Positive Long catId) {
        log.info("[PublicCategoryController] GET /categories/{}", catId);

        CategoryDto category = categoryService.getById(catId);
        log.debug("[PublicCategoryController] Категория найдена: id={}, name='{}'",
                catId, category.getName());

        return category;
    }
}