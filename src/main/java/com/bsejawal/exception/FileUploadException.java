package com.bsejawal.exception;

/**
 * Thrown when a file upload to S3 fails for an application-level reason
 * (network, permission, IO error, etc.).
 */
public class FileUploadException extends RuntimeException {

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
