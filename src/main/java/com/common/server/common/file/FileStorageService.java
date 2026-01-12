package com.common.server.common.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 파일 스토리지 서비스 인터페이스
 *
 * 다양한 스토리지 구현체를 지원하기 위한 추상화 인터페이스
 * - 로컬 파일 시스템
 * - AWS S3
 * - GCP Cloud Storage
 * - Azure Blob Storage
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
public interface FileStorageService {

    /**
     * 파일 업로드
     *
     * @param file 업로드할 파일
     * @param directory 저장할 디렉토리 경로
     * @return 저장된 파일의 URL 또는 경로
     */
    String upload(MultipartFile file, String directory);

    /**
     * 파일 업로드 (커스텀 파일명)
     *
     * @param file 업로드할 파일
     * @param directory 저장할 디렉토리 경로
     * @param filename 저장할 파일명
     * @return 저장된 파일의 URL 또는 경로
     */
    String upload(MultipartFile file, String directory, String filename);

    /**
     * InputStream으로 파일 업로드
     *
     * @param inputStream 파일 스트림
     * @param directory 저장할 디렉토리 경로
     * @param filename 저장할 파일명
     * @param contentType MIME 타입
     * @return 저장된 파일의 URL 또는 경로
     */
    String upload(InputStream inputStream, String directory, String filename, String contentType);

    /**
     * 파일 다운로드
     *
     * @param filePath 파일 경로
     * @return 파일 데이터
     */
    byte[] download(String filePath);

    /**
     * 파일 삭제
     *
     * @param filePath 파일 경로
     * @return 삭제 성공 여부
     */
    boolean delete(String filePath);

    /**
     * 파일 존재 여부 확인
     *
     * @param filePath 파일 경로
     * @return 존재 여부
     */
    boolean exists(String filePath);

    /**
     * 파일 URL 생성 (외부 접근용)
     *
     * @param filePath 파일 경로
     * @return 접근 가능한 URL
     */
    String getUrl(String filePath);

    /**
     * 임시 서명된 URL 생성 (만료 시간 있음)
     *
     * @param filePath 파일 경로
     * @param expirationMinutes 만료 시간 (분)
     * @return 서명된 URL
     */
    String getSignedUrl(String filePath, int expirationMinutes);
}
