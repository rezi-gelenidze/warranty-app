package com.warrantyvault.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentType {

    RECEIPT("Receipt"),
    WARRANTY_CARD("Warranty card"),
    MANUAL("Manual"),
    OTHER("Other");

    private final String label;
}
