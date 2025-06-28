package com.metenkanich.fastfoodkiosk.presentation.validation;

import com.metenkanich.fastfoodkiosk.persistence.entity.Category;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CategoryRepositoryImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class CategoryValidator {
  // Константи для валідації
  private static final int MIN_NAME_LENGTH = 2;
  private static final int MAX_NAME_LENGTH = 50;

  // Патерн для перевірки імені категорії
  private static final String NAME_PATTERN = "^[a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ\\s-]+$";

  // Перевірка ідентифікатора категорії
  public static ValidationResult isCategoryIdValid(UUID categoryId, boolean isExisting) {
    if (isExisting && categoryId == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Ідентифікатор категорії не може бути відсутнім для існуючої категорії");
      return new ValidationResult(false, errors);
    }
    return new ValidationResult(true);
  }

  // Перевірка назви категорії
  public static ValidationResult isNameValid(String name) {
    List<String> errors = new ArrayList<>();
    if (name == null) {
      errors.add("Назва категорії не може бути відсутньою");
      return new ValidationResult(false, errors);
    }
    if (name.length() < MIN_NAME_LENGTH) {
      errors.add("Назва \"" + name + "\" повинна містити щонайменше " + MIN_NAME_LENGTH + " символи");
    }
    if (name.length() > MAX_NAME_LENGTH) {
      errors.add("Назва \"" + name + "\" не може перевищувати " + MAX_NAME_LENGTH + " символів");
    }
    if (!Pattern.matches(NAME_PATTERN, name)) {
      errors.add("Назва \"" + name + "\" може містити лише літери, пробіли та дефіси");
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  // Перевірка унікальності назви категорії
  public static ValidationResult isNameUnique(String name, UUID categoryId, CategoryRepositoryImpl repository) {
    ValidationResult nameValidation = isNameValid(name);
    if (!nameValidation.isValid()) {
      return nameValidation;
    }

    List<String> errors = new ArrayList<>();
    try {
      // Спробуємо знайти категорію з такою ж назвою
      Category existingCategory = null;
      try {
        existingCategory = repository.findByName(name);
      } catch (Exception e) {
        // Якщо категорію не знайдено, це нормально для нової категорії
        if (!e.getMessage().contains("не знайдено")) {
          throw e; // Перекидаємо інші помилки
        }
      }

      // Перевіряємо, чи знайдена категорія не є тією самою, що ми редагуємо
      if (existingCategory != null) {
        // Якщо ми створюємо нову категорію (categoryId == null) або
        // редагуємо існуючу, але знайдена категорія має інший ID
        if (categoryId == null || !existingCategory.categoryId().equals(categoryId)) {
          errors.add("Назва \"" + name + "\" вже використовується іншою категорією");
          return new ValidationResult(false, errors);
        }
      }
    } catch (Exception e) {
      // Якщо виникла помилка при пошуку, але не "не знайдено"
      errors.add("Помилка перевірки унікальності назви: " + e.getMessage());
      return new ValidationResult(false, errors);
    }

    return new ValidationResult(true);
  }

  // Повна валідація об'єкта Category
  public static ValidationResult isCategoryValid(Category category, boolean isExisting, CategoryRepositoryImpl repository) {
    if (category == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Категорія не може бути відсутньою");
      return new ValidationResult(false, errors);
    }

    List<String> errors = new ArrayList<>();

    // Перевірка ідентифікатора
    ValidationResult categoryIdResult = isCategoryIdValid(category.categoryId(), isExisting);
    if (!categoryIdResult.isValid()) {
      errors.addAll(categoryIdResult.getErrors());
    }

    // Перевірка назви
    ValidationResult nameResult = isNameValid(category.categoryName());
    if (!nameResult.isValid()) {
      errors.addAll(nameResult.getErrors());
    }

    // Перевірка унікальності назви
    ValidationResult nameUniqueResult = isNameUnique(category.categoryName(), category.categoryId(), repository);
    if (!nameUniqueResult.isValid()) {
      errors.addAll(nameUniqueResult.getErrors());
    }

    // Повернення результату валідації
    return new ValidationResult(errors.isEmpty(), errors);
  }
}