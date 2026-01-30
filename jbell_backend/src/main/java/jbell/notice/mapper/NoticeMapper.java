package jbell.notice.mapper;

import java.util.List;
import java.util.Map; // 추가

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jbell.notice.dto.NoticeDTO;
import jbell.notice.entity.Notice;

@Mapper
public interface NoticeMapper {
    
    List<Notice> selectNoticeList();

    // 목록 (검색 파라미터 포함 가능하도록 구성)
    List<NoticeDTO> selectNoticeDTOList(@Param("keyword") String keyword, @Param("contentType") String contentType);

    // 상세
    NoticeDTO selectNoticeDTOById(Long id);

    // 등록, 수정, 삭제, 조회수
    void createNotice(Notice notice);
    void updateNotice(Notice notice);
    void deleteNotice(Long id);
    void increaseViews(Long id);

    // 공지사항 타입 목록 조회 (추가된 메서드)
    List<Map<String, Object>> selectNoticeTypes();
}