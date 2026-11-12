package com.warrantyvault.service;

import com.warrantyvault.dto.DocumentFile;
import com.warrantyvault.entity.Document;
import com.warrantyvault.entity.DocumentType;
import com.warrantyvault.entity.Purchase;
import com.warrantyvault.exception.InvalidFileException;
import com.warrantyvault.exception.NotFoundException;
import com.warrantyvault.repository.DocumentRepository;
import com.warrantyvault.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "application/pdf", ".pdf",
            "image/jpeg", ".jpg",
            "image/png", ".png");

    private static final int MAX_NAME_LENGTH = 255;

    private final DocumentRepository documentRepository;
    private final PurchaseRepository purchaseRepository;
    private final StorageService storageService;

    public List<Document> findForPurchase(Long purchaseId) {
        return documentRepository.findByPurchaseIdOrderByUploadedAtDesc(purchaseId);
    }

    @Transactional
    public Document upload(Long purchaseId, DocumentType type, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Choose a file to upload.");
        }
        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new InvalidFileException("Only PDF, JPG or PNG files are allowed.");
        }
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> NotFoundException.of("Purchase", purchaseId));

        String key = "purchases/" + purchaseId + "/" + UUID.randomUUID() + extension;
        storageService.put(key, file);
        storageService.deleteOnRollback(key);

        Document document = new Document(purchase, type, displayName(file.getOriginalFilename(), extension),
                key, file.getContentType(), file.getSize());
        return documentRepository.save(document);
    }

    public DocumentFile open(Long purchaseId, Long documentId) {
        Document document = find(purchaseId, documentId);
        return new DocumentFile(document.getOriginalName(), document.getContentType(),
                document.getSizeBytes(), storageService.open(document.getObjectKey()));
    }

    @Transactional
    public void delete(Long purchaseId, Long documentId) {
        Document document = find(purchaseId, documentId);
        documentRepository.delete(document);
        storageService.deleteAfterCommit(List.of(document.getObjectKey()));
    }

    private Document find(Long purchaseId, Long documentId) {
        return documentRepository.findByIdAndPurchaseId(documentId, purchaseId)
                .orElseThrow(() -> NotFoundException.of("Document", documentId));
    }

    private static String displayName(String originalName, String fallbackExtension) {
        String name = StringUtils.getFilename(StringUtils.cleanPath(originalName == null ? "" : originalName));
        if (!StringUtils.hasText(name)) {
            name = "document" + fallbackExtension;
        }
        return name.length() > MAX_NAME_LENGTH ? name.substring(name.length() - MAX_NAME_LENGTH) : name;
    }
}
