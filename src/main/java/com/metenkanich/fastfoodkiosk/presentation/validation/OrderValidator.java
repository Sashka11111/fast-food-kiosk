package com.metenkanich.fastfoodkiosk.presentation.validation;

import com.metenkanich.fastfoodkiosk.persistence.entity.Order;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderValidator {
  public static ValidationResult isOrderIdValid(UUID orderId, boolean isExisting) {
    if (isExisting && orderId == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Ідентифікатор замовлення не може бути відсутнім для існуючого замовлення");
      return new ValidationResult(false, errors);
    }
    return new ValidationResult(true);
  }

  public static ValidationResult isTotalPriceValid(BigDecimal totalPrice) {
    List<String> errors = new ArrayList<>();
    if (totalPrice == null) {
      errors.add("Загальна сума замовлення не може бути відсутньою");
      return new ValidationResult(false, errors);
    }
    if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
      errors.add("Загальна сума замовлення не може бути від'ємною");
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  public static ValidationResult isOrderValid(Order order, boolean isExisting) {
    if (order == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Замовлення не може бути відсутнім");
      return new ValidationResult(false, errors);
    }

    List<String> errors = new ArrayList<>();

    ValidationResult orderIdResult = isOrderIdValid(order.orderId(), isExisting);
    if (!orderIdResult.isValid()) {
      errors.addAll(orderIdResult.getErrors());
    }

    if (order.userId() == null) {
      errors.add("Ідентифікатор користувача не може бути відсутнім");
    }

    ValidationResult totalPriceResult = isTotalPriceValid(order.totalPrice());
    if (!totalPriceResult.isValid()) {
      errors.addAll(totalPriceResult.getErrors());
    }

    if (order.status() == null) {
      errors.add("Статус замовлення не може бути відсутнім");
    }

    if (order.createdAt() == null) {
      errors.add("Дата створення замовлення не може бути відсутньою");
    }

    return new ValidationResult(errors.isEmpty(), errors);
  }
}