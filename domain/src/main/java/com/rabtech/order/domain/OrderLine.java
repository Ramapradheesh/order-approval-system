
package com.rabtech.order.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderLine(
        String productId,
        int quantity,
        BigDecimal unitPrice
) {

    public OrderLine {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException(
                    "Product ID cannot be empty"
            );
        }

        productId = productId.trim();

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be a positive whole number"
            );
        }

        Objects.requireNonNull(
                unitPrice,
                "Unit price cannot be null"
        );

        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Unit price cannot be negative"
            );
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
