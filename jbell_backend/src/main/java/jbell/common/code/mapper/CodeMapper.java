package jbell.common.code.mapper;

import java.util.List;
import jbell.common.code.domain.CodeGroup;
import jbell.common.code.domain.CodeItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CodeMapper {
    // 1. 전체 코드 그룹 목록 조회
    List<CodeGroup> selectCodeGroupList();

    // 2. 특정 그룹에 속한 상세 코드(아이템) 목록 조회 (View 버튼 클릭 시 사용)
    List<CodeItem> selectCodeItemListByGroupId(String codeGroupId);
    
    // ServiceImpl에서 호출하는 메서드들
    void insertCodeGroup(CodeGroup group);
    void updateCodeGroup(CodeGroup group);
    void deleteCodeGroup(String codeGroupId);
}