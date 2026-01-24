package ru.practicum.explorewithme.server.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.category.dto.NewCategoryDto;
import ru.practicum.explorewithme.server.exception.EntityNotFoundException;
import ru.practicum.explorewithme.server.repository.CategoryRepository;
import ru.practicum.explorewithme.server.entity.Category;
import ru.practicum.explorewithme.server.repository.EventRepository;
import ru.practicum.explorewithme.server.mapper.CategoryMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String CATEGORY_NOT_FOUND = "Категория с id=%d не найдена";
    private static final String CATEGORY_NAME_EXISTS = "Название категории уже существует: %s";

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto newCategory) {
        log.info("[CategoryService] Создание категории: {}", newCategory.getName());

        // Проверка существования имени
        if (categoryRepository.existsByName(newCategory.getName())) {
            log.warn("[CategoryService] Категория с именем '{}' уже существует", newCategory.getName());
            throw new IllegalStateException(String.format(CATEGORY_NAME_EXISTS, newCategory.getName()));
        }

        Category category = categoryMapper.toEntity(newCategory);
        category = categoryRepository.save(category);
        entityManager.flush();

        log.info("[CategoryService] Категория создана: id={}, name={}", category.getId(), category.getName());
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto update(Long catId, CategoryDto categoryDto) {
        log.info("[CategoryService] Обновление категории: id={}, newName={}", catId, categoryDto.getName());

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> {
                    log.error("[CategoryService] Категория не найдена: id={}", catId);
                    return new EntityNotFoundException(String.format(CATEGORY_NOT_FOUND, catId));
                });

        // Проверка уникальности имени
        if (categoryRepository.existsByNameAndIdNot(categoryDto.getName(), catId)) {
            log.warn("[CategoryService] Имя категории уже занято: {}", categoryDto.getName());
            throw new IllegalStateException(String.format(CATEGORY_NAME_EXISTS, categoryDto.getName()));
        }

        category.setName(categoryDto.getName());
        category = categoryRepository.save(category);

        log.info("[CategoryService] Категория обновлена: id={}", catId);
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public void delete(Long catId) {
        log.info("[CategoryService] Удаление категории: id={}", catId);

        // Проверка существования
        if (!categoryRepository.existsById(catId)) {
            log.error("[CategoryService] Категория не найдена для удаления: id={}", catId);
            throw new EntityNotFoundException(String.format(CATEGORY_NOT_FOUND, catId));
        }

        // Проверка связанных событий
        if (eventRepository.existsByCategoryId(catId)) {
            log.warn("[CategoryService] Нельзя удалить категорию с событиями: id={}", catId);
            throw new IllegalStateException("Категория не пустая");
        }

        categoryRepository.deleteById(catId);
        log.info("[CategoryService] Категория удалена: id={}", catId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAll(Integer from, Integer size) {
        log.debug("[CategoryService] Получение всех категорий: from={}, size={}", from, size);

        PageRequest pageable = PageRequest.of(from / size, size);
        Page<Category> page = categoryRepository.findAll(pageable);
        List<Category> categories = page.getContent();

        log.debug("[CategoryService] Найдено {} категорий", categories.size());
        return categories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Long catId) {
        log.debug("[CategoryService] Получение категории по ID: {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> {
                    log.error("[CategoryService] Категория не найдена: id={}", catId);
                    return new EntityNotFoundException(String.format(CATEGORY_NOT_FOUND, catId));
                });

        log.debug("[CategoryService] Категория найдена: id={}, name={}", catId, category.getName());
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getEntityById(Long catId) {
        log.debug("[CategoryService] Получение entity категории: id={}", catId);

        return categoryRepository.findById(catId)
                .orElseThrow(() -> {
                    log.error("[CategoryService] Entity категории не найдена: id={}", catId);
                    return new EntityNotFoundException(String.format(CATEGORY_NOT_FOUND, catId));
                });
    }
}