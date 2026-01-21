package ru.practicum.explorewithme.server.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.server.dto.category.CategoryDto;
import ru.practicum.explorewithme.server.service.CategoryService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    // GET /categories
    // Получение категорий с пагинацией
    @GetMapping
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /categories?from={}&size={}", from, size);
        return categoryService.getCategories(from, size);
    }

    // GET /categories/{catId}
    // Получение информации о категории по её идентификатору
    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable Long catId) {
        log.info("GET /categories/{}", catId);
        return categoryService.getCategoryById(catId);
    }
}