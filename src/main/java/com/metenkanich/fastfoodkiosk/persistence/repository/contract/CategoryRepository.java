package com.metenkanich.fastfoodkiosk.persistence.repository.contract;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Category;
import java.util.List;
import java.util.UUID;

public interface CategoryRepository {
  Category findById(UUID id) throws EntityNotFoundException;
  Category findByName(String categoryName) throws EntityNotFoundException;
  List<Category> findAll();
  Category create(Category category);
  Category update(Category category) throws EntityNotFoundException;
  void deleteById(UUID id) throws EntityNotFoundException;
}