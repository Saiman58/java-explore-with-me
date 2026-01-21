package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.server.dto.category.CategoryDto;
import ru.practicum.explorewithme.server.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.server.service.CategoryService;

@Slf4j
@Valid
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    // POST /admin/categories
    // Добавление новой категории
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("POST /admin/categories: {}", newCategoryDto.getName());
        return categoryService.createCategory(newCategoryDto);
    }

    // PATCH /admin/categories/{catId}
    // Изменение категории
    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody CategoryDto categoryDto) {
        log.info("PATCH /admin/categories/{}: {}", catId, categoryDto.getName());
        return categoryService.updateCategory(catId, categoryDto);
    }

    // DELETE /admin/categories/{catId}
    // Удаление категории
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("DELETE /admin/categories/{}", catId);
        categoryService.deleteCategory(catId);
    }
}