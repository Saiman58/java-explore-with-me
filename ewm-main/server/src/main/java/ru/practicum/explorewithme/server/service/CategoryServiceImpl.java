package ru.practicum.explorewithme.server.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.server.dto.category.CategoryDto;
import ru.practicum.explorewithme.server.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.server.entity.Category;
import ru.practicum.explorewithme.server.exception.ConflictException;
import ru.practicum.explorewithme.server.exception.NotFoundException;
import ru.practicum.explorewithme.server.mapper.CategoryMapper;
import ru.practicum.explorewithme.server.repository.CategoryRepository;
import ru.practicum.explorewithme.server.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Создание категории: {}", newCategoryDto.getName());

        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Категория с именем '" + newCategoryDto.getName() + "' уже существует");
        }

        Category category = categoryMapper.toEntity(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);

        log.info("Категория создана с id={}", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("Удаление категории с id={}", categoryId);

        // Проверяем существование категории
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + categoryId + " не найдена"));

        // Проверяем, нет ли связанных событий
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("Невозможно удалить категорию, так как с ней связаны события");
        }

        categoryRepository.delete(category);
        log.info("Категория с id={} удалена", categoryId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        log.info("Обновление категории с id={}: {}", categoryId, categoryDto.getName());

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + categoryId + " не найдена"));

        // Проверяем уникальность имени, только если оно изменилось
        if (!category.getName().equals(categoryDto.getName())
                && categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Категория с именем '" + categoryDto.getName() + "' уже существует");
        }

        categoryMapper.updateEntityFromDto(categoryDto, category);
        Category updatedCategory = categoryRepository.save(category);

        log.info("Категория с id={} обновлена", categoryId);
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        log.info("Получение категорий: from={}, size={}", from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAllBy(pageable);

        return categories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        log.info("Получение категории по id={}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + categoryId + " не найдена"));

        return categoryMapper.toDto(category);
    }
}