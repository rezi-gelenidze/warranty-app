package com.warrantyvault.repository;

import com.warrantyvault.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByPurchaseIdOrderByUploadedAtDesc(Long purchaseId);

    Optional<Document> findByIdAndPurchaseId(Long id, Long purchaseId);
}
