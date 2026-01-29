package jbell.common.code.service;

import java.util.List;
import jbell.common.code.dto.CodeGroupDTO;
import jbell.common.code.dto.CodeItemDTO;

public interface CodeService {
    // 조회
	List<CodeItemDTO> getIntegratedList();
    List<CodeGroupDTO> getCodeGroupList();
    List<CodeItemDTO> getCodeItemList(String codeGroupId);
    
    // 특정 그룹 상세 정보 조회
    CodeGroupDTO getCodeGroup(String id);

    // 특정 상세 코드(아이템) 단일 조회
    CodeItemDTO getCodeItem(String groupId, String itemId);
    
    // 그룹 중복 체크를 ID와 명칭으로 분리
    boolean isGroupCodeDuplicate(String code); // ID 중복 확인
    boolean isGroupNameDuplicate(String name); // 명칭 중복 확인

    // 상세 중복 체크를 ID와 명칭으로 분리
    boolean isSubCodeDuplicate(String groupCode, String subCode); // 상세코드 ID 중복 확인
    boolean isSubNameDuplicate(String groupCode, String subName); // 상세코드 명칭 중복 확인
    
    // 등록, 수정, 삭제
    void registerGroup(CodeGroupDTO dto);
    void modifyGroup(CodeGroupDTO dto);
    void removeGroup(String id);
    void registerItem(CodeItemDTO dto);
    void modifyItem(CodeItemDTO dto);
    void removeItem(String groupId, String itemId);
    

    
   
}