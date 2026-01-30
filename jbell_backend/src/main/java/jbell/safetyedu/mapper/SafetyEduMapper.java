package jbell.safetyedu.mapper;

import jbell.safetyedu.domain.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SafetyEduMapper {
    // 목록 조회 (검색 + 페이징)
    List<Content> selectSafetyEduList(@Param("keyword") String keyword,
                                      @Param("searchType") String searchType,
                                      @Param("isPublic") String isPublic,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    // 전체 개수 조회 (페이징용)
    long countSafetyEduList(@Param("keyword") String keyword,
                            @Param("searchType") String searchType,
                            @Param("isPublic") String isPublic);

    // 상세 조회
    Content selectContentById(Long contentId);

    // 등록
    void insertContent(Content content);

    // 수정
    void updateContent(Content content);

    // 단일 삭제
    void deleteContent(Long contentId);

    // 일괄 삭제
    void deleteContents(@Param("ids") List<Long> ids);

    // 일괄 노출 상태 변경
    void updateVisibility(@Param("ids") List<Long> ids, @Param("visibleYn") String visibleYn);
    
    // ADMIN 등급을 가진 유효한 사용자 ID 하나 조회
    String selectRandomAdminId();
}