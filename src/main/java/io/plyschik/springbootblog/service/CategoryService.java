package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.CategoryDto;
import io.plyschik.springbootblog.dto.CategoryWithPostsCount;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.exception.CategoryAlreadyExistsException;
import io.plyschik.springbootblog.exception.CategoryNotFoundException;
import io.plyschik.springbootblog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;

    public Category getCategoryById(long id) {
        return categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);
    }

    public List<Category> getCategories(Sort sort) {
        return categoryRepository.findAll(sort);
    }

    public List<Category> getCategoriesWithPostCount(Sort sort) {
        return categoryRepository.findAll(sort);
    }

    public Page<CategoryWithPostsCount> getCategoriesWithPostCount(String query, Pageable pageable) {
        if (!query.isBlank()) {
            return categoryRepository.findAllWithPostsCount(query, pageable);
        }

        return categoryRepository.findAllWithPostsCount(pageable);
    }

    public List<CategoryWithPostsCount> getTop5CategoriesWithPostsCount() {
        return categoryRepository.findTop5WithPostsCount();
    }

    public List<CategoryWithPostsCount> getCategoriesWithPostsCountDashboard(Sort sort) {
        return categoryRepository.findAllWithPostsCount(sort);
    }

    public void createCategory(CategoryDto categoryDto) throws CategoryAlreadyExistsException {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new CategoryAlreadyExistsException();
        }

        Category category = modelMapper.map(categoryDto, Category.class);
        categoryRepository.save(category);
    }

    public CategoryDto getCategoryForEdit(long id) throws CategoryNotFoundException {
        Category category = categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);

        return modelMapper.map(category, CategoryDto.class);
    }

    public void updateCategory(long id, CategoryDto categoryDto) throws CategoryAlreadyExistsException, CategoryNotFoundException {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new CategoryAlreadyExistsException();
        }

        Category category = categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);
        modelMapper.map(categoryDto, category);

        categoryRepository.save(category);
    }

    public void deleteCategory(long id) {
        categoryRepository.deleteById(id);
    }
}
