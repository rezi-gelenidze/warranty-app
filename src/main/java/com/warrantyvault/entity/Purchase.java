package com.warrantyvault.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false)
    private int warrantyMonths;

    @Column(nullable = false)
    private LocalDate warrantyUntil;

    @Column(length = 1000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToMany
    @JoinTable(name = "purchase_tag",
            joinColumns = @JoinColumn(name = "purchase_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @BatchSize(size = 50)
    private Set<Tag> tags = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Purchase() {
    }

    public void update(String name, BigDecimal price, LocalDate purchaseDate, int warrantyMonths,
                       String notes, Store store, Set<Tag> tags) {
        this.name = name;
        this.price = price;
        this.purchaseDate = purchaseDate;
        this.warrantyMonths = warrantyMonths;
        this.warrantyUntil = purchaseDate.plusMonths(warrantyMonths);
        this.notes = notes;
        this.store = store;
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public LocalDate getWarrantyUntil() {
        return warrantyUntil;
    }

    public String getNotes() {
        return notes;
    }

    public Store getStore() {
        return store;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
