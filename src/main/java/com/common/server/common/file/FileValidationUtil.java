package com.common.server.common.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 파일 검증 유틸리티
 *
 * 파일 업로드 시 유효성 검사를 수행합니다.
 *
 * <p><strong>보안 주의사항:</strong>
 * 확장자와 Content-Type은 스푸핑될 수 있으므로 Magic Number 검증도 함께 수행합니다.</p>
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

    // 문서 MIME 타입
    public static final List<String> DOCUMENT_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/csv"
    );

    /**
     * 파일 Magic Number (Signature) 매핑
     * 파일의 실제 바이너리 데이터 시작 부분을 검사하여 위장된 파일을 탐지합니다.
     */
    private static final Map<String, byte[][]> MAGIC_NUMBERS = Map.ofEntries(
            // 이미지
            Map.entry("image/jpeg", new byte[][] { {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF} }),
            Map.entry("image/png", new byte[][] { {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A} }),
            Map.entry("image/gif", new byte[][] { {0x47, 0x49, 0x46, 0x38, 0x37, 0x61}, {0x47, 0x49, 0x46, 0x38, 0x39, 0x61} }), // GIF87a, GIF89a
            Map.entry("image/webp", new byte[][] { {0x52, 0x49, 0x46, 0x46} }), // RIFF (WebP container)
            Map.entry("image/bmp", new byte[][] { {0x42, 0x4D} }), // BM

            // 문서
            Map.entry("application/pdf", new byte[][] { {0x25, 0x50, 0x44, 0x46} }), // %PDF
            Map.entry("application/msword", new byte[][] { {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0} }), // DOC (OLE2)
            Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    new byte[][] { {0x50, 0x4B, 0x03, 0x04} }), // DOCX (ZIP)
            Map.entry("application/vnd.ms-excel", new byte[][] { {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0} }), // XLS (OLE2)
            Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    new byte[][] { {0x50, 0x4B, 0x03, 0x04} }), // XLSX (ZIP)
            Map.entry("application/vnd.ms-powerpoint", new byte[][] { {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0} }), // PPT (OLE2)
            Map.entry("application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    new byte[][] { {0x50, 0x4B, 0x03, 0x04} }) // PPTX (ZIP)
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
     * 이미지 파일 검증 (확장자 + MIME Type + Magic Number)
     */
    public static void validateImage(MultipartFile file) {
        validateImage(file, DEFAULT_MAX_FILE_SIZE);
    }

    /**
     * 이미지 파일 검증 (크기 지정, 확장자 + MIME Type + Magic Number)
     *
     * @param file 업로드된 파일
     * @param maxSize 최대 파일 크기 (바이트)
     * @throws IllegalArgumentException 유효하지 않은 이미지 파일인 경우
     */
    public static void validateImage(MultipartFile file, long maxSize) {
        validate(file, maxSize, IMAGE_EXTENSIONS);

        String contentType = file.getContentType();
        if (contentType == null || !IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid image content type: " + contentType);
        }

        // SVG는 텍스트 기반이므로 Magic Number 검증 제외
        if (!"image/svg+xml".equalsIgnoreCase(contentType)) {
            validateMagicNumber(file, contentType);
        }
    }

    /**
     * 문서 파일 검증 (확장자 + MIME Type + Magic Number)
     */
    public static void validateDocument(MultipartFile file) {
        validateDocument(file, DEFAULT_MAX_FILE_SIZE * 5); // 50MB
    }

    /**
     * 문서 파일 검증 (크기 지정)
     *
     * @param file 업로드된 파일
     * @param maxSize 최대 파일 크기 (바이트)
     * @throws IllegalArgumentException 유효하지 않은 문서 파일인 경우
     */
    public static void validateDocument(MultipartFile file, long maxSize) {
        validate(file, maxSize, DOCUMENT_EXTENSIONS);

        String contentType = file.getContentType();
        if (contentType == null || !DOCUMENT_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid document content type: " + contentType);
        }

        // 텍스트 기반 파일은 Magic Number 검증 제외
        if (!"text/plain".equalsIgnoreCase(contentType) && !"text/csv".equalsIgnoreCase(contentType)) {
            validateMagicNumber(file, contentType);
        }
    }

    /**
     * Magic Number (파일 시그니처) 검증
     *
     * 파일의 실제 바이너리 데이터를 확인하여 확장자/MIME Type 스푸핑을 방지합니다.
     *
     * @param file 업로드된 파일
     * @param contentType 검증할 MIME Type
     * @throws IllegalArgumentException Magic Number가 일치하지 않는 경우
     */
    public static void validateMagicNumber(MultipartFile file, String contentType) {
        byte[][] expectedSignatures = MAGIC_NUMBERS.get(contentType.toLowerCase());
        if (expectedSignatures == null) {
            // Magic Number가 정의되지 않은 타입은 검증 스킵
            return;
        }

        try (InputStream is = file.getInputStream()) {
            // 가장 긴 시그니처 길이만큼 읽기
            int maxLength = 0;
            for (byte[] sig : expectedSignatures) {
                maxLength = Math.max(maxLength, sig.length);
            }

            byte[] fileHeader = new byte[maxLength];
            int bytesRead = is.read(fileHeader);

            if (bytesRead < 0) {
                throw new IllegalArgumentException("Empty file content");
            }

            // 시그니처 중 하나라도 일치하면 통과
            for (byte[] signature : expectedSignatures) {
                if (bytesRead >= signature.length && startsWith(fileHeader, signature)) {
                    return;
                }
            }

            throw new IllegalArgumentException(
                    "File content does not match declared content type. " +
                    "Expected: " + contentType + ", but file signature does not match.");
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read file content for validation", e);
        }
    }

    /**
     * 바이트 배열이 특정 시그니처로 시작하는지 확인
     */
    private static boolean startsWith(byte[] data, byte[] signature) {
        if (data.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }
        return true;
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
