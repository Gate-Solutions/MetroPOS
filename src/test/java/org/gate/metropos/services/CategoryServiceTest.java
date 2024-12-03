package org.gate.metropos.services;

import org.gate.metropos.models.Category;
import org.gate.metropos.repositories.CategoryRepository;
import org.gate.metropos.utils.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category mockCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockCategory = Category.builder()
                .id(1L)
                .name("Test Category")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(mockCategory);
        when(categoryRepository.createCategory("Test Category")).thenReturn(mockCategory);
        when(categoryRepository.updateCategory(any(Category.class))).thenReturn(mockCategory);
    }

    @Test
    void createCategorySuccessful() {
        when(categoryRepository.findByName(any())).thenReturn(null);

        ServiceResponse<Category> response = categoryService.createCategory("Test Category");

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
    }

    @Test
    void createCategoryWithEmptyName() {
        ServiceResponse<Category> response = categoryService.createCategory("");

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Category name cannot be empty", response.getMessage());
    }

    @Test
    void createCategoryWithExistingName() {
        when(categoryRepository.findByName("Test Category")).thenReturn(mockCategory);

        ServiceResponse<Category> response = categoryService.createCategory("Test Category");

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Category with this name already exists", response.getMessage());
    }

    @Test
    void getCategorySuccessful() {
        ServiceResponse<Category> response = categoryService.getCategory(1L);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
    }

    @Test
    void getCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(null);

        ServiceResponse<Category> response = categoryService.getCategory(999L);

        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
    }

    @Test
    void getAllCategoriesSuccessful() {
        when(categoryRepository.getAllCategories()).thenReturn(Arrays.asList(mockCategory));

        ServiceResponse<List<Category>> response = categoryService.getAllCategories();

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().size());
    }

    @Test
    void deleteCategorySuccessful() {
        when(categoryRepository.deleteCategory(1L)).thenReturn(true);

        ServiceResponse<Void> response = categoryService.deleteCategory(1L);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
    }

    @Test
    void deleteCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(null);

        ServiceResponse<Void> response = categoryService.deleteCategory(999L);

        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
    }

    @Test
    void updateCategorySuccessful() {
        ServiceResponse<Category> response = categoryService.updateCategory(mockCategory);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
    }

    @Test
    void updateCategoryWithNullId() {
        mockCategory.setId(null);

        ServiceResponse<Category> response = categoryService.updateCategory(mockCategory);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Category ID cannot be null", response.getMessage());
    }

    @Test
    void updateCategoryWithEmptyName() {
        mockCategory.setName("");

        ServiceResponse<Category> response = categoryService.updateCategory(mockCategory);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Category name cannot be empty", response.getMessage());
    }
}
