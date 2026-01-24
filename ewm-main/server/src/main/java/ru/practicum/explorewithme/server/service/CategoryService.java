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
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String CATEGORY_NOT_FOUND = "Категория с id=%d не найдена";
    private static final String CATEGORY_NAME_EXISTS = "Название категории уже существует: %s";

    @Transactional
    public CategoryDto create(NewCategoryDto newCategory) {
        log.info("Создание категории с именем '{}'", newCategory.getName());
        if (categoryRepository.existsByName(newCategory.getName())) {
            throw new IllegalStateException(String.format(CATEGORY_NAME_EXISTS, newCategory.getName()));
        }
        Category category = categoryMapper.toEntity(newCategory);
        category = categoryRepository.save(category);
        entityManager.flush();
        log.info("Категория создана с ID {}", category.getId());
        return categoryMapper.toDto(category);
    }

    public CategoryDto update(Long catId, CategoryDto categoryDto) {
        log.info("Обновление категории ID {} с именем '{}'", catId, categoryDto.getName());

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(CATEGORY_NOT_FOUND, catId)
                ));

        if (categoryRepository.existsByNameAndIdNot(categoryDto.getName(), catId)) {
            throw new IllegalStateException(
                    String.format(CATEGORY_NAME_EXISTS, categoryDto.getName())
            );
        }

        category.setName(categoryDto.getName());
        category = categoryRepository.save(category);

        log.info("Категория ID {} обновлена", catId);
        return categoryMapper.toDto(category);
    }

    public void delete(Long catId) {
        log.info("Удаление категории ID {}", catId);

        if (!categoryRepository.existsById(catId)) {
            throw new EntityNotFoundException(
                    String.format(CATEGORY_NOT_FOUND, catId)
            );
        }

        if (eventRepository.existsByCategoryId(catId)) {
            throw new IllegalStateException("Категория не пустая");
        }

        categoryRepository.deleteById(catId);
        log.info("Категория ID {} удалена", catId);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAll(Integer from, Integer size) {
        log.debug("Получение всех категорий from {} size {}", from, size);
        PageRequest pageable = PageRequest.of(from / size, size);
        Page<Category> page = categoryRepository.findAll(pageable);
        List<Category> categories = page.getContent();
        return categories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getById(Long catId) {
        log.debug("Получение категории ID {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(CATEGORY_NOT_FOUND, catId)
                ));

        return categoryMapper.toDto(category);
    }

    @Transactional(readOnly = true)
    public Category getEntityById(Long catId) {
        log.debug("Получение entity категории ID {}", catId);

        return categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(CATEGORY_NOT_FOUND, catId)
                ));
    }
}