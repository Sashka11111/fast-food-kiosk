package com.metenkanich.fastfoodkiosk.presentation.validation;

import com.metenkanich.fastfoodkiosk.persistence.entity.Cart;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CartRepositoryImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartValidator {
    // Константи для валідації
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 100;
    private static final double MIN_SUBTOTAL = 0.01;
    private static final double MAX_SUBTOTAL = 100000.00;

    // Перевірка ідентифікатора кошика
    public static ValidationResult isCartIdValid(UUID cartId, boolean isExisting) {
        if (isExisting && cartId == null) {
            List<String> errors = new ArrayList<>();
            errors.add("Ідентифікатор кошика не може бути відсутнім для існуючого кошика");
            return new ValidationResult(false, errors);
        }
        return new ValidationResult(true);
    }

    // Перевірка ідентифікатора користувача
    public static ValidationResult isUserIdValid(UUID userId) {
        List<String> errors = new ArrayList<>();
        if (userId == null) {
            errors.add("Ідентифікатор користувача не може бути відсутнім");
            return new ValidationResult(false, errors);
        }
        return new ValidationResult(true);
    }

    // Перевірка ідентифікатора елемента
    public static ValidationResult isItemIdValid(UUID itemId) {
        List<String> errors = new ArrayList<>();
        if (itemId == null) {
            errors.add("Ідентифікатор елемента не може бути відсутнім");
            return new ValidationResult(false, errors);
        }
        return new ValidationResult(true);
    }

    // Перевірка кількості
    public static ValidationResult isQuantityValid(int quantity) {
        List<String> errors = new ArrayList<>();
        if (quantity < MIN_QUANTITY) {
            errors.add("Кількість (" + quantity + ") повинна бути не меншою за " + MIN_QUANTITY);
        }
        if (quantity > MAX_QUANTITY) {
            errors.add("Кількість (" + quantity + ") не може перевищувати " + MAX_QUANTITY);
        }
        return new ValidationResult(errors.isEmpty(), errors);
    }

    // Перевірка субтоталу
    public static ValidationResult isSubtotalValid(double subtotal) {
        List<String> errors = new ArrayList<>();
        if (subtotal < MIN_SUBTOTAL) {
            errors.add("Субтотал (" + subtotal + ") повинен бути не меншим за " + MIN_SUBTOTAL);
        }
        if (subtotal > MAX_SUBTOTAL) {
            errors.add("Субтотал (" + subtotal + ") не може перевищувати " + MAX_SUBTOTAL);
        }
        return new ValidationResult(errors.isEmpty(), errors);
    }
    // Повна валідація об'єкта Cart
    public static ValidationResult isCartValid(Cart cart, boolean isExisting, CartRepositoryImpl repository) {
        if (cart == null) {
            List<String> errors = new ArrayList<>();
            errors.add("Кошик не може бути відсутнім");
            return new ValidationResult(false, errors);
        }

        List<String> errors = new ArrayList<>();

        // Перевірка ідентифікатора кошика
        ValidationResult cartIdResult = isCartIdValid(cart.cartId(), isExisting);
        if (!cartIdResult.isValid()) {
            errors.addAll(cartIdResult.getErrors());
        }

        // Перевірка ідентифікатора користувача
        ValidationResult userIdResult = isUserIdValid(cart.userId());
        if (!userIdResult.isValid()) {
            errors.addAll(userIdResult.getErrors());
        }

        // Перевірка ідентифікатора елемента
        ValidationResult itemIdResult = isItemIdValid(cart.itemId());
        if (!itemIdResult.isValid()) {
            errors.addAll(itemIdResult.getErrors());
        }

        // Перевірка кількості
        ValidationResult quantityResult = isQuantityValid(cart.quantity());
        if (!quantityResult.isValid()) {
            errors.addAll(quantityResult.getErrors());
        }

        // Перевірка субтоталу
        ValidationResult subtotalResult = isSubtotalValid(cart.subtotal());
        if (!subtotalResult.isValid()) {
            errors.addAll(subtotalResult.getErrors());
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}