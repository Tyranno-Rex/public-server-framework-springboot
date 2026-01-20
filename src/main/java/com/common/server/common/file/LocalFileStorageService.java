package com.common.server.common.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 로컬 파일 시스템 스토리지 구현체
 *
 * 개발 환경 또는 단일 서버 환경에서 사용합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.base-url:http://localhost:8080/files}")
    private String baseUrl;

    @Override
    public String upload(MultipartFile file, String directory) {
        String filename = generateUniqueFilename(file.getOriginalFilename());
        return upload(file, directory, filename);
    }

    @Override
    public String upload(MultipartFile file, String directory, String filename) {
        try {
            return upload(file.getInputStream(), directory, filename, file.getContentType());
        } catch (IOException e) {
            log.error("Failed to upload file: {}", filename, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public String upload(InputStream inputStream, String directory, String filename, String contentType) {
        try {
            Path directoryPath = Paths.get(uploadPath, directory);
            Files.createDirectories(directoryPath);

            Path filePath = directoryPath.resolve(filename);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = directory + "/" + filename;
            log.info("File uploaded: {}", relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("Failed to upload file: {}", filename, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public byte[] download(String filePath) {
        try {
            Path path = resolveSafePath(filePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Failed to download file: {}", filePath, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    @Override
    public boolean delete(String filePath) {
        try {
            Path path = resolveSafePath(filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("File deleted: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    @Override
    public boolean exists(String filePath) {
        try {
            Path path = resolveSafePath(filePath);
            return Files.exists(path);
        } catch (SecurityException e) {
            log.warn("Invalid file path attempted: {}", filePath);
            return false;
        }
    }

    /**
     * Path Traversal 공격 방지를 위한 안전한 경로 검증
     *
     * ../나 ..\ 같은 시퀀스를 사용한 디렉토리 탈출 공격을 방지합니다.
     *
     * @param filePath 검증할 파일 경로
     * @return 검증된 안전한 Path
     * @throws SecurityException 경로가 업로드 디렉토리 외부를 가리키는 경우
     */
    private Path resolveSafePath(String filePath) {
        try {
            Path basePath = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path resolvedPath = basePath.resolve(filePath).toAbsolutePath().normalize();

            // 경로가 기본 디렉토리 내에 있는지 확인
            if (!resolvedPath.startsWith(basePath)) {
                log.warn("Path traversal attempt detected: {} (resolved to: {})", filePath, resolvedPath);
                throw new SecurityException("Invalid file path: path traversal detected");
            }

            return resolvedPath;
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to resolve path: {}", filePath, e);
            throw new SecurityException("Invalid file path", e);
        }
    }

    @Override
    public String getUrl(String filePath) {
        return baseUrl + "/" + filePath;
    }

    @Override
    public String getSignedUrl(String filePath, int expirationMinutes) {
        // 로컬 스토리지에서는 단순 URL 반환
        return getUrl(filePath);
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
