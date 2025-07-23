package com.metenkanich.fastfoodkiosk.presentation.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private final boolean isValid;
    private final List<String> errors;

    public ValidationResult(boolean isValid) {
        this.isValid = isValid;
        this.errors = new ArrayList<>();
    }

    public ValidationResult(boolean isValid, List<String> errors) {
        this.isValid = isValid;
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
    }

    public boolean isValid() {
        return isValid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getErrorMessage() {
        if (errors.isEmpty()) {
            return "";
        }
        StringBuilder formattedErrors = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            formattedErrors.append("• ").append(errors.get(i));
            if (i < errors.size() - 1) {
                formattedErrors.append("\n");
            }
        }
        return formattedErrors.toString();
    }
}