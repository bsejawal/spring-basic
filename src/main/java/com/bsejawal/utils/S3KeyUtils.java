package com.bsejawal.utils;

import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * Helpers for building safe, unique S3 object keys.
 */
@UtilityClass
public class S3KeyUtils {

    /**
     * Build a unique S3 key as {@code <folder>/<uuid>-<sanitized-original-name>}.
     * Folder may be null/blank. Original filename may be null/blank.
     */
    public String buildKey(String folder, String originalFilename) {
        String fileName = (originalFilename == null || originalFilename.isBlank())
                ? UUID.randomUUID().toString()
                : UUID.randomUUID() + "-" + sanitize(originalFilename);
        String prefix = (folder == null || folder.isBlank())
                ? ""
                : (folder.endsWith("/") ? folder : folder + "/");
        return prefix + fileName;
    }

    /** Replace path separators and whitespace so the file name is S3-key-safe. */
    private String sanitize(String name) {
        return name.replaceAll("[\\s\\\\/]+", "_");
    }
}
