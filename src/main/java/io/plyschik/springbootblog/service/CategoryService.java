package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.CategoryDto;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.exception.CategoryAlreadyExists;
import io.plyschik.springbootblog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public void createCategory(CategoryDto categoryDto) throws CategoryAlreadyExists {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new CategoryAlreadyExists();
        }

        Category category = new Category();
        category.setName(categoryDto.getName());

        categoryRepository.save(category);
    }
}
