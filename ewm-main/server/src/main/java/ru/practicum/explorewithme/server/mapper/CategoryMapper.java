package ru.practicum.explorewithme.server.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.server.dto.category.CategoryDto;
import ru.practicum.explorewithme.server.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.server.entity.Category;

@Component
public class CategoryMapper {

    // Преобразование NewCategoryDto в Category (для создания)
    public Category toEntity(NewCategoryDto newCategoryDto) {
        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

    // Преобразование Category в CategoryDto (для отдачи польз)
    public CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    // Обновление Category из CategoryDto (для частичного обновления)
    public void updateEntityFromDto(CategoryDto categoryDto, Category category) {
        // Обновляем только name, id не трогаем
        if (categoryDto.getName() != null && !categoryDto.getName().isBlank()) {
            category.setName(categoryDto.getName());
        }
    }
}