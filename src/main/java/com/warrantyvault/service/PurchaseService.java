package com.warrantyvault.service;

import com.warrantyvault.dto.PurchaseForm;
import com.warrantyvault.dto.PurchaseRow;
import com.warrantyvault.entity.Document;
import com.warrantyvault.entity.Purchase;
import com.warrantyvault.entity.Store;
import com.warrantyvault.entity.Tag;
import com.warrantyvault.exception.NotFoundException;
import com.warrantyvault.repository.DocumentRepository;
import com.warrantyvault.repository.PurchaseRepository;
import com.warrantyvault.repository.StoreRepository;
import com.warrantyvault.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class PurchaseService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("purchaseDate", "price", "warrantyUntil", "name");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "purchaseDate");

    private final PurchaseRepository purchaseRepository;
    private final StoreRepository storeRepository;
    private final TagRepository tagRepository;
    private final DocumentRepository documentRepository;
    private final StorageService storageService;

    public PurchaseService(PurchaseRepository purchaseRepository, StoreRepository storeRepository,
                           TagRepository tagRepository, DocumentRepository documentRepository,
                           StorageService storageService) {
        this.purchaseRepository = purchaseRepository;
        this.storeRepository = storeRepository;
        this.tagRepository = tagRepository;
        this.documentRepository = documentRepository;
        this.storageService = storageService;
    }

    public Page<PurchaseRow> search(String query, Long storeId, Long tagId, Pageable pageable) {
        LocalDate today = LocalDate.now();
        String text = query == null ? "" : query.strip();
        return purchaseRepository.search(text, storeId, tagId, withSafeSort(pageable))
                .map(purchase -> PurchaseRow.from(purchase, today));
    }

    public List<PurchaseRow> findEndingSoon() {
        LocalDate today = LocalDate.now();
        return purchaseRepository.findWarrantiesEndingBetween(today, today.plusDays(PurchaseRow.ENDING_SOON_DAYS))
                .stream()
                .map(purchase -> PurchaseRow.from(purchase, today))
                .toList();
    }

    public Purchase getDetails(Long id) {
        return purchaseRepository.findWithStoreAndTagsById(id)
                .orElseThrow(() -> NotFoundException.of("Purchase", id));
    }

    public PurchaseForm getForm(Long id) {
        return PurchaseForm.from(getDetails(id));
    }

    public List<Store> findAllStores() {
        return storeRepository.findAllByOrderByNameAsc();
    }

    public List<Tag> findAllTags() {
        return tagRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public Long create(PurchaseForm form) {
        Purchase purchase = new Purchase();
        apply(form, purchase);
        return purchaseRepository.save(purchase).getId();
    }

    @Transactional
    public void update(Long id, PurchaseForm form) {
        apply(form, getDetails(id));
    }

    @Transactional
    public void delete(Long id) {
        Purchase purchase = getDetails(id);
        List<Document> documents = documentRepository.findByPurchaseIdOrderByUploadedAtDesc(id);
        documentRepository.deleteAll(documents);
        purchaseRepository.delete(purchase);
        storageService.deleteAfterCommit(documents.stream().map(Document::getObjectKey).toList());
    }

    private void apply(PurchaseForm form, Purchase purchase) {
        Store store = storeRepository.findById(form.getStoreId())
                .orElseThrow(() -> NotFoundException.of("Store", form.getStoreId()));
        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(form.getTagIds()));
        purchase.update(form.getName().strip(), form.getPrice(), form.getPurchaseDate(),
                form.getWarrantyMonths(), blankToNull(form.getNotes()), store, tags);
    }

    private static Pageable withSafeSort(Pageable pageable) {
        Sort sort = Sort.by(pageable.getSort().stream()
                .filter(order -> SORTABLE_FIELDS.contains(order.getProperty()))
                .toList());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                sort.isSorted() ? sort : DEFAULT_SORT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
