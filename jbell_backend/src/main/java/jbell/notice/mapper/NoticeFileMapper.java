package jbell.notice.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jbell.notice.dto.NoticeFileDTO;
import jbell.notice.entity.NoticeFile;

@Mapper
public interface NoticeFileMapper {
	 // 파일 등록
    void insertFile(NoticeFile file);
    
    // 게시글의 파일 목록 조회
    List<NoticeFileDTO> selectFilesByNoticeId(@Param("contentId") Long contentId);
    
    // 파일 개별 삭제
    void deleteFile(@Param("fileId") Long fileId);
    
    // 게시글의 모든 파일 삭제
    void deleteFilesByNoticeId(@Param("noticeId") Long noticeId);
    
    // 파일 개수 조회
    int countFilesByNoticeId(@Param("noticeId") Long noticeId);
    
    // 파일 ID로 파일 정보 조회 (다운로드용)
    NoticeFileDTO selectFileById(@Param("fileId") Long fileId);
}
