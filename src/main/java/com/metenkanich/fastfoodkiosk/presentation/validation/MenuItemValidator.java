package com.metenkanich.fastfoodkiosk.presentation.validation;

import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.MenuItemRepositoryImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class MenuItemValidator {
  private static final int MIN_NAME_LENGTH = 2;
  private static final int MAX_NAME_LENGTH = 100;
  private static final int MAX_DESCRIPTION_LENGTH = 500;
  private static final String NAME_PATTERN = "^[a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ\\s-]+$";

  public static ValidationResult isItemIdValid(UUID itemId, boolean isExisting) {
    if (isExisting && itemId == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Ідентифікатор пункту меню не може бути відсутнім для існуючого пункту");
      return new ValidationResult(false, errors);
    }
    return new ValidationResult(true);
  }

  public static ValidationResult isNameValid(String name) {
    List<String> errors = new ArrayList<>();
    if (name == null) {
      errors.add("Назва пункту меню не може бути відсутньою");
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

  public static ValidationResult isDescriptionValid(String description) {
    List<String> errors = new ArrayList<>();
    if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
      errors.add("Опис не може перевищувати " + MAX_DESCRIPTION_LENGTH + " символів");
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  public static ValidationResult isPriceValid(BigDecimal price) {
    List<String> errors = new ArrayList<>();
    if (price == null) {
      errors.add("Ціна пункту меню не може бути відсутньою");
      return new ValidationResult(false, errors);
    }
    if (price.compareTo(BigDecimal.ZERO) <= 0) {
      errors.add("Ціна пункту меню повинна бути більше нуля");
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  public static ValidationResult isNameUnique(String name, UUID itemId, MenuItemRepositoryImpl repository) {
    ValidationResult nameValidation = isNameValid(name);
    if (!nameValidation.isValid()) {
      return nameValidation;
    }

    List<String> errors = new ArrayList<>();
    try {
      MenuItem existingItem = null;
      try {
        existingItem = repository.findByName(name);
      } catch (Exception e) {
        if (!e.getMessage().contains("не знайдено")) {
          throw e;
        }
      }

      if (existingItem != null) {
        if (itemId == null || !existingItem.itemId().equals(itemId)) {
          errors.add("Назва \"" + name + "\" вже використовується іншим пунктом меню");
          return new ValidationResult(false, errors);
        }
      }
    } catch (Exception e) {
      errors.add("Помилка перевірки унікальності назви: " + e.getMessage());
      return new ValidationResult(false, errors);
    }

    return new ValidationResult(true);
  }

  public static ValidationResult isMenuItemValid(MenuItem menuItem, boolean isExisting, MenuItemRepositoryImpl repository) {
    if (menuItem == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Пункт меню не може бути відсутнім");
      return new ValidationResult(false, errors);
    }

    List<String> errors = new ArrayList<>();

    ValidationResult itemIdResult = isItemIdValid(menuItem.itemId(), isExisting);
    if (!itemIdResult.isValid()) {
      errors.addAll(itemIdResult.getErrors());
    }

    ValidationResult nameResult = isNameValid(menuItem.name());
    if (!nameResult.isValid()) {
      errors.addAll(nameResult.getErrors());
    }

    ValidationResult nameUniqueResult = isNameUnique(menuItem.name(), menuItem.itemId(), repository);
    if (!nameUniqueResult.isValid()) {
      errors.addAll(nameUniqueResult.getErrors());
    }

    ValidationResult descriptionResult = isDescriptionValid(menuItem.description());
    if (!descriptionResult.isValid()) {
      errors.addAll(descriptionResult.getErrors());
    }

    ValidationResult priceResult = isPriceValid(menuItem.price());
    if (!priceResult.isValid()) {
      errors.addAll(priceResult.getErrors());
    }

    if (menuItem.categoryId() == null) {
      errors.add("Ідентифікатор категорії не може бути відсутнім");
    }

    if (menuItem.isAvailable() == null) {
      errors.add("Статус доступності не може бути відсутнім");
    }

    return new ValidationResult(errors.isEmpty(), errors);
  }
}