package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.CategoryDto;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.exception.CategoryAlreadyExistsException;
import io.plyschik.springbootblog.exception.CategoryNotFoundException;
import io.plyschik.springbootblog.repository.CategoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    public void getCategoryByIdShouldCallFindByIdMethodFromCategoryRepositoryAndReturnCategoryWhenExists() {
        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));

        Category category = categoryService.getCategoryById(1);

        Assertions.assertNotNull(category);
        Mockito.verify(categoryRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    public void getCategoryByIdShouldCallFindByIdMethodFromCategoryRepositoryAndThrowCategoryNotFoundExceptionWhenNotExists() {
        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(
            CategoryNotFoundException.class,
            () -> categoryService.getCategoryById(1)
        );

        Mockito.verify(categoryRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    public void getCategoriesShouldReturnSortedCategoriesList() {
        Mockito.when(categoryRepository.findAll(Mockito.any(Sort.class))).thenReturn(new ArrayList<>());

        categoryService.getCategories(Sort.unsorted());

        Mockito.verify(categoryRepository, Mockito.times(1)).findAll(Sort.unsorted());
    }

    @Test
    public void getCategoriesWithPostsCountShouldReturnSortedCategoriesListWithPostsCount() {
        Mockito.when(categoryRepository.findAllWithPostsCount(Mockito.any(Sort.class))).thenReturn(new ArrayList<>());

        categoryService.getCategoriesWithPostsCount(Sort.unsorted());

        Mockito.verify(categoryRepository, Mockito.times(1)).findAllWithPostsCount(Sort.unsorted());
    }

    @Test
    public void getCategoriesWithPostsCountAndWithQueryShouldReturnSortedCategoriesListWithPostsCount() {
        Mockito.when(categoryRepository.findAllWithPostsCount("Test", Pageable.unpaged()))
            .thenReturn(new PageImpl<>(new ArrayList<>()));

        categoryService.getCategoriesWithPostCount("Test", Pageable.unpaged());

        Mockito.verify(categoryRepository, Mockito.times(1))
            .findAllWithPostsCount("Test", Pageable.unpaged());
    }

    @Test
    public void getCategoriesWithPostsCountAndWithoutQueryShouldReturnSortedCategoriesListWithPostsCount() {
        Mockito.when(categoryRepository.findAllWithPostsCount(Pageable.unpaged()))
            .thenReturn(new PageImpl<>(new ArrayList<>()));

        categoryService.getCategoriesWithPostCount("", Pageable.unpaged());

        Mockito.verify(categoryRepository, Mockito.times(1))
            .findAllWithPostsCount(Pageable.unpaged());
    }

    @Test
    public void getTop5CategoriesWithPostsCountShouldReturnTop5CategoriesListWithPostsCount() {
        Mockito.when(categoryRepository.findTop5WithPostsCount()).thenReturn(new ArrayList<>());

        categoryService.getTop5CategoriesWithPostsCount();

        Mockito.verify(categoryRepository, Mockito.times(1)).findTop5WithPostsCount();
    }

    @Test
    public void createCategoryShouldThrowCategoryAlreadyExistsExceptionWhenCategoryNameIsNotUnique() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Test");

        Mockito.when(categoryRepository.existsByName(categoryDto.getName())).thenReturn(true);

        Assertions.assertThrows(
            CategoryAlreadyExistsException.class,
            () -> categoryService.createCategory(categoryDto)
        );

        Mockito.verify(categoryRepository, Mockito.times(1)).existsByName(categoryDto.getName());
    }

    @Test
    public void createCategoryShouldSaveCategoryWhenCategoryNameIsUnique() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Test");

        Category category = new Category();

        Mockito.when(categoryRepository.existsByName(categoryDto.getName())).thenReturn(false);
        Mockito.when(modelMapper.map(categoryDto, Category.class)).thenReturn(category);
        Mockito.when(categoryRepository.save(Mockito.any(Category.class))).thenAnswer(i -> i.getArguments()[0]);

        categoryService.createCategory(categoryDto);

        Mockito.verify(categoryRepository, Mockito.times(1)).existsByName(categoryDto.getName());
        Mockito.verify(categoryRepository, Mockito.times(1)).save(category);
    }

    @Test
    public void getCategoryForEditShouldCallFindByIdMethodFromCategoryRepositoryAndReturnCategoryDtoWhenExists() {
        Category category = new Category();

        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        Mockito.when(modelMapper.map(category, CategoryDto.class)).thenReturn(new CategoryDto());

        CategoryDto categoryDto = categoryService.getCategoryForEdit(1);

        Assertions.assertNotNull(categoryDto);
        Mockito.verify(categoryRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    public void getCategoryForEditShouldCallFindByIdMethodFromCategoryRepositoryAndThrowCategoryNotFoundExceptionWhenNotExists() {
        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(
            CategoryNotFoundException.class,
            () -> categoryService.getCategoryForEdit(1)
        );

        Mockito.verify(categoryRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    public void updateCategoryShouldThrowCategoryAlreadyExistsExceptionWhenCategoryNameIsNotUnique() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Test");

        Mockito.when(categoryRepository.existsByName(categoryDto.getName())).thenReturn(true);

        Assertions.assertThrows(
            CategoryAlreadyExistsException.class,
            () -> categoryService.updateCategory(1, categoryDto)
        );

        Mockito.verify(categoryRepository, Mockito.times(1)).existsByName(categoryDto.getName());
    }

    @Test
    public void updateCategoryShouldThrowCategoryNotExistsExceptionWhenCategoryNotExists() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Test");

        Mockito.when(categoryRepository.existsByName(categoryDto.getName())).thenReturn(false);
        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(
            CategoryNotFoundException.class,
            () -> categoryService.updateCategory(1, categoryDto)
        );

        Mockito.verify(categoryRepository, Mockito.times(1)).existsByName(categoryDto.getName());
        Mockito.verify(categoryRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    public void updateCategoryUpdateCategoryWhenNameIsUnique() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Test");

        Category category = new Category();
        category.setName("Test");

        Mockito.when(categoryRepository.existsByName(categoryDto.getName())).thenReturn(false);
        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        Mockito.when(categoryRepository.save(Mockito.any(Category.class))).thenAnswer(i -> i.getArguments()[0]);

        categoryService.updateCategory(1, categoryDto);

        Mockito.verify(categoryRepository, Mockito.times(1)).existsByName(categoryDto.getName());
        Mockito.verify(categoryRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(categoryRepository, Mockito.times(1)).save(category);
    }

    @Test
    public void deleteCategoryShouldDeleteCategoryById() {
        categoryService.deleteCategory(1);

        Mockito.verify(categoryRepository, Mockito.times(1)).deleteById(1L);
    }
}
