package org.gate.metropos.services;

import lombok.AllArgsConstructor;
import org.gate.metropos.models.Category;
import org.gate.metropos.repositories.CategoryRepository;
import org.gate.metropos.utils.ServiceResponse;

import java.util.List;

@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }

    public ServiceResponse<Category> createCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return new ServiceResponse<>(false, 400, "Category name cannot be empty", null);
        }

        Category newCategory = categoryRepository.createCategory(category);
        return new ServiceResponse<>(true, 200, "Category created successfully", newCategory);
    }

    public ServiceResponse<Category> getCategory(Long id) {
        Category category = categoryRepository.findById(id);
        if (category == null) {
            return new ServiceResponse<>(false, 404, "Category not found", null);
        }
        return new ServiceResponse<>(true, 200, "Category retrieved successfully", category);
    }

    public ServiceResponse<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.getAllCategories();
        return new ServiceResponse<>(true, 200, "Categories retrieved successfully", categories);
    }
}