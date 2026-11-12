package com.warrantyvault.dto;

import com.warrantyvault.entity.Purchase;
import com.warrantyvault.entity.Tag;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class PurchaseForm {

    private static final int DEFAULT_WARRANTY_MONTHS = 24;

    @NotBlank(message = "Item name is required.")
    @Size(max = 120, message = "Item name can be at most 120 characters.")
    private String name;

    @NotNull(message = "Choose a store.")
    private Long storeId;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.00", message = "Price must be 0 or more.")
    @Digits(integer = 10, fraction = 2, message = "Price can have at most 2 decimal places.")
    private BigDecimal price;

    @NotNull(message = "Purchase date is required.")
    @PastOrPresent(message = "Purchase date cannot be in the future.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchaseDate;

    @NotNull(message = "Warranty length is required.")
    @Min(value = 0, message = "Warranty must be 0 months or more.")
    @Max(value = 120, message = "Warranty can be at most 120 months.")
    private Integer warrantyMonths;

    private Set<Long> tagIds = new HashSet<>();

    @Size(max = 1000, message = "Notes can be at most 1000 characters.")
    private String notes;

    public static PurchaseForm empty() {
        PurchaseForm form = new PurchaseForm();
        form.setPurchaseDate(LocalDate.now());
        form.setWarrantyMonths(DEFAULT_WARRANTY_MONTHS);
        return form;
    }

    public static PurchaseForm from(Purchase purchase) {
        PurchaseForm form = new PurchaseForm();
        form.setName(purchase.getName());
        form.setStoreId(purchase.getStore().getId());
        form.setPrice(purchase.getPrice());
        form.setPurchaseDate(purchase.getPurchaseDate());
        form.setWarrantyMonths(purchase.getWarrantyMonths());
        form.setTagIds(purchase.getTags().stream().map(Tag::getId).collect(Collectors.toSet()));
        form.setNotes(purchase.getNotes());
        return form;
    }

    public void setTagIds(Set<Long> tagIds) {
        this.tagIds = tagIds == null ? new HashSet<>() : tagIds;
    }
}
