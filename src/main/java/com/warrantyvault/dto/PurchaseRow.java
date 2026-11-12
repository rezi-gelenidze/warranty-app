package com.warrantyvault.dto;

import com.warrantyvault.entity.Purchase;
import com.warrantyvault.entity.Tag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

public record PurchaseRow(
        Long id,
        String name,
        String storeName,
        String tagNames,
        LocalDate purchaseDate,
        BigDecimal price,
        LocalDate warrantyUntil,
        long daysLeft) {

    public static final int ENDING_SOON_DAYS = 30;

    public static PurchaseRow from(Purchase purchase, LocalDate today) {
        String tagNames = purchase.getTags().stream()
                .map(Tag::getName)
                .sorted()
                .collect(Collectors.joining(", "));
        return new PurchaseRow(
                purchase.getId(),
                purchase.getName(),
                purchase.getStore().getName(),
                tagNames,
                purchase.getPurchaseDate(),
                purchase.getPrice(),
                purchase.getWarrantyUntil(),
                ChronoUnit.DAYS.between(today, purchase.getWarrantyUntil()));
    }

    public boolean expired() {
        return daysLeft < 0;
    }

    public boolean endingSoon() {
        return daysLeft >= 0 && daysLeft <= ENDING_SOON_DAYS;
    }
}
