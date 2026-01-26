package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.category.dto.NewCategoryDto;
import ru.practicum.explorewithme.server.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Validated
public class AdminCategoryController {
    private final CategoryService categoryService;

    // POST /admin/categories
    // Добавление новой категории
    @PostMapping
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody NewCategoryDto newCategory) {
        log.info("[AdminCategoryController] POST /admin/categories: name='{}'", newCategory.getName());

        CategoryDto created = categoryService.create(newCategory);
        log.info("[AdminCategoryController] Категория создана: id={}, name={}",
                created.getId(), created.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET /admin/categories
    // Получение категорий
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAll(@RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                    @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("[AdminCategoryController] GET /admin/categories?from={}&size={}", from, size);

        List<CategoryDto> categories = categoryService.getAll(from, size);
        log.debug("[AdminCategoryController] Найдено {} категорий", categories.size());

        return ResponseEntity.ok(categories);
    }

    // PATCH /admin/categories/{catId}
    // Изменение категории
    @PatchMapping("/{catId}")
    public CategoryDto update(@PathVariable @Positive Long catId,
                              @Valid @RequestBody CategoryDto categoryDto) {

        log.info("[AdminCategoryController] PATCH /admin/categories/{}", catId);
        log.debug("[AdminCategoryController] Обновление категории: oldName->'{}', newName->'{}'",
                catId, categoryDto.getName());

        CategoryDto updated = categoryService.update(catId, categoryDto);
        log.info("[AdminCategoryController] Категория обновлена: id={}, name={}",
                catId, updated.getName());

        return updated;
    }

    // DELETE /admin/categories/{catId}
    // Удаление категории
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long catId) {
        log.info("[AdminCategoryController] DELETE /admin/categories/{}", catId);

        categoryService.delete(catId);
        log.info("[AdminCategoryController] Категория удалена: id={}", catId);
    }
}