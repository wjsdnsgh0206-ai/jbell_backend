package jbell.common.utils;

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
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jbell.common.domain.FileMetaData;


@Component
public class FilesUtils {

	@Value("${file.path}")
	private String fileRealPath;
	
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	// 파일인덱스 생성
	private static final DateTimeFormatter FILEIDX_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
	
	
	public FileMetaData uploadFile(MultipartFile multipartFile) {
		
		FileMetaData fileInfo = storeFile(multipartFile);
		
		return fileInfo;
	}

	public List<FileMetaData> uploadFiles(List<MultipartFile> multipartFiles) {
		List<FileMetaData> fileList = new ArrayList<FileMetaData>();
		FileMetaData fileInfo;
		for(MultipartFile multipartFile : multipartFiles) {
			fileInfo = storeFile(multipartFile);
			if(fileInfo != null) fileList.add(fileInfo);
		}
		return fileList;
	}

	
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
	
	private FileMetaData storeFile(MultipartFile multipartFile) {
		if(multipartFile.isEmpty()) return null;
		
		// 현재 날짜 기준으로 디렉토리 이름 
		LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
		String dateDirectory  = now.format(DATE_FORMATTER);
		
		// 파일 분류
		String contentType = multipartFile.getContentType();
		String typeDirectory = (contentType != null && contentType.contains("image")) ? "image" : "files";
		
		// 경로 설정
		Path folderPath = Paths.get(fileRealPath, "attachment", dateDirectory, typeDirectory);
		// 디렉토리 생성
		createFolder(folderPath);
		
		String originalFileName = multipartFile.getOriginalFilename();
		String extension = getExtension(originalFileName);
		String newFileName = UUID.randomUUID() + extension;
		// OS환경에 맞춰 경로 / \  설정 
		Path uploadPath = folderPath.resolve(newFileName);
		
		FileMetaData fileMetaData = null;
		
		try {
			// 파일 저장
			Files.write(uploadPath, multipartFile.getBytes());
			
			// 파일 정보 생성 생성안함
			//String fileIdx = now.format(FILEIDX_DATE_FORMATTER) + UUID.randomUUID().toString().replace("-", "").subSequence(0, 16);
			
			fileMetaData = FileMetaData.builder()
									   .fileOriginalName(originalFileName)
									   .fileNewName(newFileName)
									   //이거 \ 경로 부터 교체 해야 됨 아님 /home/teamproject 치환 안됨
									   .filePath("/api" + uploadPath.toString().replace("\\", "/").replace(fileRealPath, ""))
									   .fileSize(multipartFile.getSize())
									   .fileType(extension)
									   .build();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return fileMetaData;

	}
	
	public boolean deleteFileByPath(String filePath) {
		Path deletePath = Paths.get(fileRealPath, filePath);
		try {
			System.out.println(deletePath);
			return Files.deleteIfExists(deletePath);
		
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
}











