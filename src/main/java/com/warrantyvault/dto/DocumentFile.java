package com.warrantyvault.dto;

import java.io.InputStream;

public record DocumentFile(String fileName, String contentType, long sizeBytes, InputStream content) {
}
