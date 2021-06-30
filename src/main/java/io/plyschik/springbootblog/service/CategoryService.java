package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.CategoryDto;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.exception.CategoryAlreadyExists;
import io.plyschik.springbootblog.exception.CategoryNotFound;
import io.plyschik.springbootblog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Optional<Category> getById(long id) {
        return categoryRepository.findById(id);
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public Page<Category> getPaginatedCategories(Pageable pageable) {
        return categoryRepository.findAllByOrderByIdDesc(pageable);
    }

    public void createCategory(CategoryDto categoryDto) throws CategoryAlreadyExists {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new CategoryAlreadyExists();
        }

        Category category = new Category();
        category.setName(categoryDto.getName());

        categoryRepository.save(category);
    }

    public CategoryDto getCategoryForEdit(long id) throws CategoryNotFound {
        Category category = categoryRepository.findById(id).orElseThrow(CategoryNotFound::new);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(category.getName());

        return categoryDto;
    }

    public void updateCategory(long id, CategoryDto categoryDto) throws CategoryAlreadyExists, CategoryNotFound {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new CategoryAlreadyExists();
        }

        Category category = categoryRepository.findById(id).orElseThrow(CategoryNotFound::new);
        category.setName(categoryDto.getName());

        categoryRepository.save(category);
    }

    public void delete(long id) throws CategoryNotFound {
        Category category = categoryRepository.findById(id).orElseThrow(CategoryNotFound::new);

        categoryRepository.delete(category);
    }
}