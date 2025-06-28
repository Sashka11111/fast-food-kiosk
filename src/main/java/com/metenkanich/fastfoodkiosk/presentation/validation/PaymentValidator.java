package com.metenkanich.fastfoodkiosk.presentation.validation;

import com.metenkanich.fastfoodkiosk.persistence.entity.Payment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class PaymentValidator {
    // Constants for validation
    private static final int MAX_PAYMENT_METHOD_LENGTH = 50;
    private static final String PAYMENT_METHOD_PATTERN = "^[a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ\\s-]+$";

    // Validate payment ID
    public static ValidationResult isPaymentIdValid(UUID id, boolean isExisting) {
        if (isExisting && id == null) {
            List<String> errors = new ArrayList<>();
            errors.add("Ідентифікатор платежу не може бути відсутнім для існуючого платежу");
            return new ValidationResult(false, errors);
        }
        return new ValidationResult(true);
    }

    // Validate amount
    public static ValidationResult isAmountValid(BigDecimal amount) {
        List<String> errors = new ArrayList<>();
        if (amount == null) {
            errors.add("Сума платежу не може бути відсутньою");
            return new ValidationResult(false, errors);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Сума платежу повинна бути більше нуля");
        }
        return new ValidationResult(errors.isEmpty(), errors);
    }

    // Validate payment method
    public static ValidationResult isPaymentMethodValid(String paymentMethod) {
        List<String> errors = new ArrayList<>();
        if (paymentMethod == null) {
            errors.add("Метод оплати не може бути відсутнім");
            return new ValidationResult(false, errors);
        }
        if (paymentMethod.length() > MAX_PAYMENT_METHOD_LENGTH) {
            errors.add("Метод оплати не може перевищувати " + MAX_PAYMENT_METHOD_LENGTH + " символів");
        }
        if (!Pattern.matches(PAYMENT_METHOD_PATTERN, paymentMethod)) {
            errors.add("Метод оплати \"" + paymentMethod + "\" може містити лише літери, пробіли та дефіси");
        }
        return new ValidationResult(errors.isEmpty(), errors);
    }

    // Full validation of Payment object
    public static ValidationResult isPaymentValid(Payment payment, boolean isExisting) {
        if (payment == null) {
            List<String> errors = new ArrayList<>();
            errors.add("Платіж не може бути відсутнім");
            return new ValidationResult(false, errors);
        }

        List<String> errors = new ArrayList<>();

        // Validate payment ID
        ValidationResult paymentIdResult = isPaymentIdValid(payment.id(), isExisting);
        if (!paymentIdResult.isValid()) {
            errors.addAll(paymentIdResult.getErrors());
        }

        // Validate order ID
        if (payment.orderId() == null) {
            errors.add("Ідентифікатор замовлення не може бути відсутнім");
        }

        // Validate amount
        ValidationResult amountResult = isAmountValid(payment.amount());
        if (!amountResult.isValid()) {
            errors.addAll(amountResult.getErrors());
        }

        // Validate payment method
        ValidationResult paymentMethodResult = isPaymentMethodValid(payment.paymentMethod());
        if (!paymentMethodResult.isValid()) {
            errors.addAll(paymentMethodResult.getErrors());
        }

        // Validate payment status
        if (payment.paymentStatus() == null) {
            errors.add("Статус платежу не може бути відсутнім");
        }

        // Validate createdAt
        if (payment.createdAt() == null) {
            errors.add("Дата створення платежу не може бути відсутньою");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}