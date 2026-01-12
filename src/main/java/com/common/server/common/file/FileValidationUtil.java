package com.common.server.common.file;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 파일 검증 유틸리티
 *
 * 파일 업로드 시 유효성 검사를 수행합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
public class FileValidationUtil {

    // 이미지 확장자
    public static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg"
    );

    // 문서 확장자
    public static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv"
    );

    // 이미지 MIME 타입
    public static final List<String> IMAGE_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp", "image/svg+xml"
    );

    // 기본 최대 파일 크기 (10MB)
    public static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;

    private FileValidationUtil() {
    }

    /**
     * 파일 유효성 검사
     */
    public static void validate(MultipartFile file, long maxSize, List<String> allowedExtensions) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds limit: " + maxSize + " bytes");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (allowedExtensions != null && !allowedExtensions.isEmpty()) {
            if (!allowedExtensions.contains(extension.toLowerCase())) {
                throw new IllegalArgumentException("File extension not allowed: " + extension);
            }
        }
    }

    /**
     * 이미지 파일 검증
     */
    public static void validateImage(MultipartFile file) {
        validate(file, DEFAULT_MAX_FILE_SIZE, IMAGE_EXTENSIONS);

        String contentType = file.getContentType();
        if (contentType == null || !IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid image content type: " + contentType);
        }
    }

    /**
     * 이미지 파일 검증 (크기 지정)
     */
    public static void validateImage(MultipartFile file, long maxSize) {
        validate(file, maxSize, IMAGE_EXTENSIONS);

        String contentType = file.getContentType();
        if (contentType == null || !IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid image content type: " + contentType);
        }
    }

    /**
     * 문서 파일 검증
     */
    public static void validateDocument(MultipartFile file) {
        validate(file, DEFAULT_MAX_FILE_SIZE * 5, DOCUMENT_EXTENSIONS); // 50MB
    }

    /**
     * 파일 확장자 추출
     */
    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 파일명 (확장자 제외) 추출
     */
    public static String getBasename(String filename) {
        if (filename == null) {
            return "";
        }
        if (!filename.contains(".")) {
            return filename;
        }
        return filename.substring(0, filename.lastIndexOf("."));
    }

    /**
     * 안전한 파일명 생성 (특수문자 제거)
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        return filename.replaceAll("[^a-zA-Z0-9가-힣._-]", "_");
    }
}
