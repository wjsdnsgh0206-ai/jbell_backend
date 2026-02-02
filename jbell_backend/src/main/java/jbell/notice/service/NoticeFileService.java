package jbell.notice.service;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jbell.notice.dto.NoticeFileDTO;
import jbell.notice.entity.NoticeFile;
import jbell.notice.mapper.NoticeFileMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeFileService {
    
    private final NoticeFileMapper fileMapper;
    
    // application.properties에서 설정 (또는 하드코딩)
    @Value("${file.upload.path:C:/uploads/notice}")
    private String uploadPath;
    
    /**
     * 파일 저장
     */
    public List<NoticeFile> saveFiles(List<MultipartFile> files, Long noticeId) throws IOException {
        List<NoticeFile> savedFiles = new ArrayList<>();
        
        // 업로드 폴더 생성
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            
            String originalName = file.getOriginalFilename(); // 원본 파일명
            String fileExt = ""; // 확장자 추출
            if (originalName != null && originalName.contains(".")) {
                fileExt = originalName.substring(originalName.lastIndexOf(".") + 1);
            }
            
            // UUID로 파일명 생성
            String savedName = UUID.randomUUID().toString() + "_" + originalName;
            String filePath = uploadPath + File.separator + savedName;
            
            // 파일 저장
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());
            
            // DB 저장
            NoticeFile noticeFile = new NoticeFile();
            noticeFile.setContentId(noticeId);      // noticeId -> contentId
            noticeFile.setFileRealName(originalName); // originalName -> fileRealName
            noticeFile.setFileName(savedName);      // savedName -> fileName
            noticeFile.setFilePath(filePath);
            noticeFile.setFileSize(file.getSize());
            noticeFile.setFileExt(fileExt);         // fileType 대신 확장자 저장
            
            fileMapper.insertFile(noticeFile);
            savedFiles.add(noticeFile);
        }
        
        return savedFiles;
    }
    
    /**
     * 파일 삭제
     */
    public void deleteFile(Long fileId) throws IOException {
        // 1. DB에서 파일 정보 조회
        NoticeFileDTO file = fileMapper.selectFileById(fileId);
        
        if (file != null) {
            // 2. 실제 파일 삭제
            File physicalFile = new File(file.getFilePath());
            if (physicalFile.exists()) {
                physicalFile.delete();
            }
            
            // 3. DB 레코드 삭제
            fileMapper.deleteFile(fileId);
        }
    }
    
    /**
     * 게시글의 모든 파일 조회
     */
    public List<NoticeFileDTO> getFilesByNoticeId(Long noticeId) {
        List<NoticeFileDTO> files = fileMapper.selectFilesByNoticeId(noticeId);
        return files != null ? files : new ArrayList<>();
    }
    
    /**
     * 파일 ID로 파일 조회
     */
    public NoticeFileDTO getFileById(Long fileId) {
        return fileMapper.selectFileById(fileId);
    }
}
