package com.programing.bookweb.service;

import com.programing.bookweb.entity.Category;

import java.util.List;

public interface ICategoryService {

    List<Category> getAllCategories();

    Category getCategoryById(Long categoryId);

    Category addCategory(Category category);

    Category updateCategory(Long categoryId, Category category);

    void deleteCategory(Long categoryId);

    void safeDeleteCategory(Long categoryId);

    boolean existsByName(Category category);

}
