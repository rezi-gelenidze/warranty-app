package com.warrantyvault.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException ex, Model model) {
        return errorPage(model, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    public String handleTooLarge(Model model) {
        return errorPage(model, HttpStatus.CONTENT_TOO_LARGE, "The file is too large. The limit is 10 MB.");
    }

    @ExceptionHandler(StorageException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleStorage(StorageException ex, Model model) {
        log.error("Object storage failed", ex);
        return errorPage(model, HttpStatus.SERVICE_UNAVAILABLE, "File storage is not available. Try again later.");
    }

    private String errorPage(Model model, HttpStatus status, String message) {
        model.addAttribute("status", status.value());
        model.addAttribute("error", status.getReasonPhrase());
        model.addAttribute("message", message);
        return "error";
    }
}
