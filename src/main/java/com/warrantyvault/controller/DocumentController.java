package com.warrantyvault.controller;

import com.warrantyvault.dto.DocumentFile;
import com.warrantyvault.entity.DocumentType;
import com.warrantyvault.exception.InvalidFileException;
import com.warrantyvault.service.DocumentService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;

@Controller
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/purchases/{purchaseId}/documents")
    public String upload(@PathVariable Long purchaseId,
                         @RequestParam("file") MultipartFile file,
                         @RequestParam("type") DocumentType type,
                         RedirectAttributes redirect) {
        try {
            documentService.upload(purchaseId, type, file);
            redirect.addFlashAttribute("success", "File uploaded.");
        } catch (InvalidFileException ex) {
            redirect.addFlashAttribute("uploadError", ex.getMessage());
        }
        return "redirect:/purchases/" + purchaseId;
    }

    @GetMapping("/purchases/{purchaseId}/documents/{documentId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long purchaseId,
                                                        @PathVariable Long documentId) {
        DocumentFile file = documentService.open(purchaseId, documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(new InputStreamResource(file.content()));
    }

    @DeleteMapping("/purchases/{purchaseId}/documents/{documentId}")
    public String delete(@PathVariable Long purchaseId, @PathVariable Long documentId,
                         RedirectAttributes redirect) {
        documentService.delete(purchaseId, documentId);
        redirect.addFlashAttribute("success", "File removed.");
        return "redirect:/purchases/" + purchaseId;
    }
}
