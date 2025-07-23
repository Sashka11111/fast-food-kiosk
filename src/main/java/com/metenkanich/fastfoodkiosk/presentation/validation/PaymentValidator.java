package com.metenkanich.fastfoodkiosk.presentation.validation;

import com.metenkanich.fastfoodkiosk.persistence.entity.Payment;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class PaymentValidator {
    private static final int MAX_PAYMENT_METHOD_LENGTH = 50;
    private static final String PAYMENT_METHOD_PATTERN = "^[a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ\\s-]+$";

    public static ValidationResult isPaymentIdValid(UUID id, boolean isExisting) {
        if (isExisting && id == null) {
            List<String> errors = new ArrayList<>();
            errors.add("Ідентифікатор платежу не може бути відсутнім для існуючого платежу");
            return new ValidationResult(false, errors);
        }
        return new ValidationResult(true);
    }



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

    public static ValidationResult isPaymentValid(Payment payment, boolean isExisting) {
        if (payment == null) {
            List<String> errors = new ArrayList<>();
            errors.add("Платіж не може бути відсутнім");
            return new ValidationResult(false, errors);
        }

        List<String> errors = new ArrayList<>();

        ValidationResult paymentIdResult = isPaymentIdValid(payment.id(), isExisting);
        if (!paymentIdResult.isValid()) {
            errors.addAll(paymentIdResult.getErrors());
        }

        if (payment.cartId() == null) {
            errors.add("Ідентифікатор кошика не може бути відсутнім");
        }

        ValidationResult paymentMethodResult = isPaymentMethodValid(payment.paymentMethod());
        if (!paymentMethodResult.isValid()) {
            errors.addAll(paymentMethodResult.getErrors());
        }

        if (payment.paymentStatus() == null) {
            errors.add("Статус платежу не може бути відсутнім");
        }

        if (payment.createdAt() == null) {
            errors.add("Дата створення платежу не може бути відсутньою");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}