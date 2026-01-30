package jbell.common.code.mapper;

import java.util.List;
import jbell.common.code.domain.CodeGroup;
import jbell.common.code.domain.CodeItem;
import jbell.common.code.dto.CodeItemDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CodeMapper {
    
    // 공통코드 통합 조회
    List<CodeItemDTO> selectCodeIntegratedList();
    
    // 그룹 관련
    List<CodeGroup> selectCodeGroupList();
    CodeGroup selectCodeGroupById(String codeGroupId);
    void insertCodeGroup(CodeGroup group);
    void updateCodeGroup(CodeGroup group);
    void deleteCodeGroup(String codeGroupId);
   // void deleteCodeItemsByGroupId(String groupId);
    
    // 상세 아이템 관련
    void insertCodeItem(CodeItem item);
    void updateCodeItem(CodeItem item);
    void deleteCodeItem(@Param("codeGroupId") String codeGroupId, 
                        @Param("codeItemId") String codeItemId);
    
    // 특정 그룹에 속한 상세 코드 리스트 조회
    List<CodeItem> selectCodeItemListByGroupId(String codeGroupId);
    
    /**
     * [수정 포인트] 반환 타입을 CodeItem에서 CodeItemDTO로 변경 
     * 서비스(Impl)에서 CodeItemDTO로 받고 있기 때문에 반드시 일치해야 합니다.
     */
    CodeItemDTO selectCodeItemById(@Param("codeGroupId") String codeGroupId, 
                                   @Param("codeItemId") String codeItemId);

    // 그룹 관련 체크
    int countGroupCode(String groupCode);
    int countGroupName(String groupName);

    // 상세 관련 체크
    int countSubCode(@Param("groupCode") String groupCode, @Param("subCode") String subCode);
    int countSubName(@Param("groupCode") String groupCode, @Param("subName") String subName);
    
    // 그룹 코드의 최대 순서 조회
    int selectMaxGroupOrder();

    // 특정 그룹 내 상세 코드의 최대 순서 조회
    int selectMaxItemOrder(@Param("groupCode") String groupCode);
}