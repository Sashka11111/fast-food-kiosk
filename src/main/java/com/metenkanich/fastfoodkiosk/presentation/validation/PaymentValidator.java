package com.metenkanich.fastfoodkiosk.presentation.validation;

import com.metenkanich.fastfoodkiosk.persistence.entity.Payment;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PaymentMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaymentValidator {
    public static ValidationResult isPaymentIdValid(UUID id, boolean isExisting) {
        if (isExisting && id == null) {
            return new ValidationResult(false, List.of(
                "Ідентифікатор платежу не може бути відсутнім для існуючого платежу"
            ));
        }
        return new ValidationResult(true);
    }

    public static ValidationResult isPaymentMethodValid(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return new ValidationResult(false, List.of(
                "Метод оплати не може бути відсутнім"
            ));
        }
        return new ValidationResult(true);
    }

    public static ValidationResult isPaymentValid(Payment payment, boolean isExisting) {
        if (payment == null) {
            return new ValidationResult(false, List.of(
                "Платіж не може бути відсутнім"
            ));
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
