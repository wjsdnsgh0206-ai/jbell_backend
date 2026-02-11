package jbell.notice.service;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
	
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	// 파일인덱스 생성
	private static final DateTimeFormatter FILEIDX_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    
    private final NoticeFileMapper fileMapper;
    
    // application.properties에서 설정 (또는 하드코딩)
    @Value("${file.path}")
    private String uploadPath;
    
   // 폴더생성
 	private void createFolder(Path path) {
 		try {
 			Files.createDirectories(path);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException("디렉토리 생성 실패: " + path, e);
 		}
 	}
 	
 	// 확장자 반환
 	private String getExtension(String fileName) {
 		// 파일명 없을 경우 빈값 반환
 		if(fileName == null) return "";
 		
 		// .마지막 인덱스확인
 		int dotIdx = fileName.lastIndexOf(".");
 		
 		// 만약 파일. 인경우는 확장자로 보기 어려우므로 file.(X) ,123.file(O)
 		return (dotIdx != -1 && dotIdx < fileName.length() - 1)
 				? "." + fileName.substring(dotIdx + 1)
 				: "";		
 	}
    
    /**
     * 파일 저장
     */
    public List<NoticeFile> saveFiles(List<MultipartFile> files, Long noticeId) throws IOException {
        List<NoticeFile> savedFiles = new ArrayList<>();
        
       
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
    		
    		// 현재 날짜 기준으로 디렉토리 이름 
    		LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
    		String dateDirectory  = now.format(DATE_FORMATTER);
    		
    		// 파일 분류
    		String contentType = file.getContentType();
    		String typeDirectory = (contentType != null && contentType.contains("image")) ? "image" : "files";
    		
    		// 경로 설정
    		Path folderPath = Paths.get(uploadPath, "attachment", dateDirectory, typeDirectory);
    		// 디렉토리 생성
    		createFolder(folderPath);
    		
    		String originalFileName = file.getOriginalFilename();
    		String extension = getExtension(originalFileName);
    		String newFileName = UUID.randomUUID() + extension;
    		// OS환경에 맞춰 경로 / \  설정 
    		Path uploadPaths = folderPath.resolve(newFileName);
            
            // 파일 저장
            Files.write(uploadPaths, file.getBytes());
            
            // DB 저장
            NoticeFile noticeFile = new NoticeFile();
            noticeFile.setContentId(noticeId);      // noticeId -> contentId
            noticeFile.setFileRealName(originalFileName); // originalName -> fileRealName
            noticeFile.setFileName(newFileName);      // savedName -> fileName
            noticeFile.setFilePath("/api" + uploadPaths.toString().replace("\\", "/").replace(uploadPath, ""));
            noticeFile.setFileSize(file.getSize());
            noticeFile.setFileExt(extension);         // fileType 대신 확장자 저장
            
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
