package com.warrantyvault.repository;

import com.warrantyvault.entity.Purchase;
import com.warrantyvault.entity.Store;
import com.warrantyvault.entity.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestConstructor;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PurchaseRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    private final PurchaseRepository purchaseRepository;
    private final StoreRepository storeRepository;
    private final TagRepository tagRepository;

    private Store store;
    private Tag phones;
    private Tag gifts;

    PurchaseRepositoryTest(PurchaseRepository purchaseRepository, StoreRepository storeRepository,
                           TagRepository tagRepository) {
        this.purchaseRepository = purchaseRepository;
        this.storeRepository = storeRepository;
        this.tagRepository = tagRepository;
    }

    @BeforeEach
    void setUp() {
        store = storeRepository.save(new Store("Test store"));
        phones = tagRepository.save(new Tag("Test phones"));
        gifts = tagRepository.save(new Tag("Test gifts"));
    }

    @Test
    void searchFiltersByTagWithoutDuplicates() {
        save("Test phone A", LocalDate.now(), 12, Set.of(phones, gifts));
        save("Test phone B", LocalDate.now(), 12, Set.of(phones));
        save("Test case", LocalDate.now(), 12, Set.of(gifts));

        Page<Purchase> page = purchaseRepository.search("test phone", store.getId(), phones.getId(),
                PageRequest.of(0, 10, Sort.by("name")));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Purchase::getName)
                .containsExactly("Test phone A", "Test phone B");
    }

    @Test
    void findWarrantiesEndingBetweenReturnsSoonestFirst() {
        LocalDate today = LocalDate.now();
        save("Test ends in 20 days", today.minusMonths(12).plusDays(20), 12, Set.of());
        save("Test ends in 5 days", today.minusMonths(12).plusDays(5), 12, Set.of());
        save("Test ends next year", today, 12, Set.of());

        List<Purchase> result = purchaseRepository.findWarrantiesEndingBetween(today, today.plusDays(30));

        assertThat(result).extracting(Purchase::getName)
                .filteredOn(name -> name.startsWith("Test"))
                .containsExactly("Test ends in 5 days", "Test ends in 20 days");
    }

    private void save(String name, LocalDate purchaseDate, int warrantyMonths, Set<Tag> tags) {
        Purchase purchase = new Purchase();
        purchase.update(name, BigDecimal.TEN, purchaseDate, warrantyMonths, null, store, tags);
        purchaseRepository.save(purchase);
    }
}
