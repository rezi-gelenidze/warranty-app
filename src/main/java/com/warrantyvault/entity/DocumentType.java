package com.warrantyvault.entity;

public enum DocumentType {

    RECEIPT("Receipt"),
    WARRANTY_CARD("Warranty card"),
    MANUAL("Manual"),
    OTHER("Other");

    private final String label;

    DocumentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
