package com.warrantyvault.controller;

import com.warrantyvault.dto.PurchaseForm;
import com.warrantyvault.dto.PurchaseRow;
import com.warrantyvault.entity.DocumentType;
import com.warrantyvault.entity.Purchase;
import com.warrantyvault.service.DocumentService;
import com.warrantyvault.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final DocumentService documentService;

    @GetMapping("/")
    public String home() {
        return "redirect:/purchases";
    }

    @GetMapping("/purchases")
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) Long storeId,
                       @RequestParam(required = false) Long tagId,
                       @PageableDefault(sort = "purchaseDate", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<PurchaseRow> page = purchaseService.search(q, storeId, tagId, pageable);
        model.addAttribute("page", page);
        model.addAttribute("endingSoon", purchaseService.findEndingSoon());
        model.addAttribute("stores", purchaseService.findAllStores());
        model.addAttribute("tags", purchaseService.findAllTags());
        model.addAttribute("q", q);
        model.addAttribute("storeId", storeId);
        model.addAttribute("tagId", tagId);
        model.addAttribute("sort", sortParam(page.getSort()));
        return "purchases/list";
    }

    @GetMapping("/purchases/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Purchase purchase = purchaseService.getDetails(id);
        model.addAttribute("purchase", purchase);
        model.addAttribute("summary", PurchaseRow.from(purchase, LocalDate.now()));
        model.addAttribute("documents", documentService.findForPurchase(id));
        model.addAttribute("documentTypes", DocumentType.values());
        return "purchases/detail";
    }

    @GetMapping("/purchases/new")
    public String newForm(Model model) {
        return showForm(model, PurchaseForm.empty(), null);
    }

    @PostMapping("/purchases")
    public String create(@Valid @ModelAttribute("purchase") PurchaseForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return showForm(model, form, null);
        }
        Long id = purchaseService.create(form);
        redirect.addFlashAttribute("success", "Purchase saved. You can add its receipt below.");
        return "redirect:/purchases/" + id;
    }

    @GetMapping("/purchases/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        return showForm(model, purchaseService.getForm(id), id);
    }

    @PutMapping("/purchases/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("purchase") PurchaseForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return showForm(model, form, id);
        }
        purchaseService.update(id, form);
        redirect.addFlashAttribute("success", "Changes saved.");
        return "redirect:/purchases/" + id;
    }

    @DeleteMapping("/purchases/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        purchaseService.delete(id);
        redirect.addFlashAttribute("success", "Purchase deleted.");
        return "redirect:/purchases";
    }

    private String showForm(Model model, PurchaseForm form, Long purchaseId) {
        model.addAttribute("purchase", form);
        model.addAttribute("purchaseId", purchaseId);
        model.addAttribute("stores", purchaseService.findAllStores());
        model.addAttribute("tags", purchaseService.findAllTags());
        return "purchases/form";
    }

    private static String sortParam(Sort sort) {
        return sort.stream()
                .findFirst()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .orElse("purchaseDate,desc");
    }
}
