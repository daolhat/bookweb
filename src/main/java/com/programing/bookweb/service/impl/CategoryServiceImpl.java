package com.programing.bookweb.service.impl;

import com.programing.bookweb.entity.Category;
import com.programing.bookweb.repository.CategoryRepository;
import com.programing.bookweb.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(Long categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        return category.orElse(null);
    }

    @Override
    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long categoryId, Category category) {

        Category existingCategory = getCategoryById(categoryId);
        if (existingCategory != null){
            existingCategory.setName(category.getName());
            existingCategory.setDescription(category.getDescription());
            return categoryRepository.save(existingCategory);
        } else {
            throw new IllegalArgumentException("Danh mục thể loại có id: " + categoryId + " không tồn tại!");
        }
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        if (category != null) {
            categoryRepository.delete(category);
        }
    }

    @Override
    public boolean existsByName(Category category) {
        return categoryRepository.existsByName(category.getName());
    }
}
