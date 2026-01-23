package jbell.common.code.service;

import java.util.List;
import jbell.common.code.dto.CodeGroupDTO;
import jbell.common.code.dto.CodeItemDTO;

public interface CodeService {
    // 조회
    List<CodeGroupDTO> getCodeGroupList();
    List<CodeItemDTO> getCodeItemList(String codeGroupId);
    
    // 등록, 수정, 삭제
    void registerGroup(CodeGroupDTO dto);
    void modifyGroup(CodeGroupDTO dto);
    void removeGroup(String id);
}