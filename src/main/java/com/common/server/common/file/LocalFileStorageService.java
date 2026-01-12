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
            Path path = Paths.get(uploadPath, filePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Failed to download file: {}", filePath, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    @Override
    public boolean delete(String filePath) {
        try {
            Path path = Paths.get(uploadPath, filePath);
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
        Path path = Paths.get(uploadPath, filePath);
        return Files.exists(path);
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
