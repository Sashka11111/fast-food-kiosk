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
  private static final int MAX_IMAGE_PATH_LENGTH = 255; // Максимальна довжина шляху до зображення

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

  // Перевірка шляху до зображення категорії
  public static ValidationResult isImagePathValid(String imagePath) {
    List<String> errors = new ArrayList<>();

    // Шлях до зображення не є обов'язковим
    if (imagePath == null || imagePath.trim().isEmpty()) {
      return new ValidationResult(true);
    }

    // Перевірка довжини шляху
    if (imagePath.length() > MAX_IMAGE_PATH_LENGTH) {
      errors.add("Шлях до зображення не може перевищувати " + MAX_IMAGE_PATH_LENGTH + " символів");
    }

    // Перевірка формату шляху (повинен бути відносним шляхом до файлу зображення)
    if (!isValidImagePath(imagePath)) {
      errors.add("Шлях до зображення повинен мати формат '/images/categories/filename.ext' та підтримувані розширення: .jpg, .jpeg, .png, .gif, .bmp");
    }

    return new ValidationResult(errors.isEmpty(), errors);
  }

  // Перевірка формату шляху до зображення
  private static boolean isValidImagePath(String imagePath) {
    if (imagePath == null || imagePath.trim().isEmpty()) {
      return false;
    }

    // Перевірка, що шлях починається з /images/categories/
    if (!imagePath.startsWith("/images/categories/")) {
      return false;
    }

    // Перевірка розширення файлу
    String lowerPath = imagePath.toLowerCase();
    return lowerPath.endsWith(".jpg") ||
           lowerPath.endsWith(".jpeg") ||
           lowerPath.endsWith(".png") ||
           lowerPath.endsWith(".gif") ||
           lowerPath.endsWith(".bmp");
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

    // Перевірка шляху до зображення
    ValidationResult imageResult = isImagePathValid(category.imagePath());
    if (!imageResult.isValid()) {
      errors.addAll(imageResult.getErrors());
    }

    // Повернення результату валідації
    return new ValidationResult(errors.isEmpty(), errors);
  }
}