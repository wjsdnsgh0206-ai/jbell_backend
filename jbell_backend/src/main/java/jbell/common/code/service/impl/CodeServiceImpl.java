package jbell.common.code.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import jbell.common.code.domain.CodeGroup;
import jbell.common.code.domain.CodeItem;
import jbell.common.code.dto.CodeGroupDTO;
import jbell.common.code.dto.CodeItemDTO;
import jbell.common.code.mapper.CodeMapper;
import jbell.common.code.service.CodeService;

@Service
@RequiredArgsConstructor
public class CodeServiceImpl implements CodeService {
    
    private final CodeMapper codeMapper;
    
    @Override
    public List<CodeItemDTO> getIntegratedList() {

        return codeMapper.selectCodeIntegratedList();
    }

    @Override
    public List<CodeGroupDTO> getCodeGroupList() {
        return codeMapper.selectCodeGroupList().stream()
                .map(g -> CodeGroupDTO.builder()
                        .groupCode(g.getCodeGroupId())
                        .groupName(g.getCodeGroupName())
                        .desc(g.getCodeDesc())
                        .order(g.getSortOrder())
                        .visible("Y".equals(g.getVisibleYn()))
                        .createdAt(g.getCreatedAt() != null ? g.getCreatedAt().toString() : null)
                        .updatedAt(g.getUpdatedAt() != null ? g.getUpdatedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public CodeGroupDTO getCodeGroup(String id) {
        CodeGroup g = codeMapper.selectCodeGroupById(id);
        if (g == null) return null;

        return CodeGroupDTO.builder()
                .groupCode(g.getCodeGroupId())
                .groupName(g.getCodeGroupName())
                .desc(g.getCodeDesc())
                .order(g.getSortOrder())
                .visible("Y".equals(g.getVisibleYn()))
                .createdAt(g.getCreatedAt() != null ? g.getCreatedAt().toString() : null)
                .updatedAt(g.getUpdatedAt() != null ? g.getUpdatedAt().toString() : null)
                .build();
    }

    @Override
    @Transactional
    public void registerGroup(CodeGroupDTO dto) {
        if (codeMapper.countGroupCode(dto.getGroupCode()) > 0) throw new RuntimeException("이미 존재하는 그룹코드입니다.");
        if (codeMapper.countGroupName(dto.getGroupName()) > 0) throw new RuntimeException("이미 존재하는 그룹명입니다.");

        if (dto.getOrder() == null || dto.getOrder() == 0) {
            int maxOrder = codeMapper.selectMaxGroupOrder();
            dto.setOrder(maxOrder + 1);
        }

        CodeGroup group = CodeGroup.builder()
                .codeGroupId(dto.getGroupCode())
                .codeGroupName(dto.getGroupName())
                .codeDesc(dto.getDesc())
                .sortOrder(dto.getOrder())
                .visibleYn(dto.isVisible() ? "Y" : "N")
                .build();
                
        codeMapper.insertCodeGroup(group);
    }

    @Override
    @Transactional
    public void modifyGroup(CodeGroupDTO dto) {
        CodeGroup existing = codeMapper.selectCodeGroupById(dto.getGroupCode());
        if (existing == null) throw new RuntimeException("수정하려는 그룹코드가 존재하지 않습니다.");

        if (!existing.getCodeGroupName().equals(dto.getGroupName())) {
            if (codeMapper.countGroupName(dto.getGroupName()) > 0) throw new RuntimeException("이미 사용 중인 그룹명입니다.");
        }

        Integer order = (dto.getOrder() == null || dto.getOrder() == 0) 
                        ? existing.getSortOrder() 
                        : dto.getOrder();

        CodeGroup group = CodeGroup.builder()
                .codeGroupId(dto.getGroupCode())
                .codeGroupName(dto.getGroupName())
                .codeDesc(dto.getDesc())
                .sortOrder(order)
                .visibleYn(dto.isVisible() ? "Y" : "N")
                .build();

        codeMapper.updateCodeGroup(group);
       
        // 그룹이 미사용(N)으로 변경된 경우, 해당 그룹의 모든 상세코드도 미사용(N) 처리
        if (!dto.isVisible()) { 
            codeMapper.updateItemsStatusByGroupId(dto.getGroupCode(), "N");
        }

    }

    @Override
    @Transactional
    public void removeGroup(String id) {
    	
        // 1. 해당 그룹에 속한 상세코드(자식)들을 먼저 모두 삭제
       codeMapper.deleteCodeItemsByGroupId(id); 

        // 2. 그 다음 그룹코드(부모) 삭제
        codeMapper.deleteCodeGroup(id);
    }

    @Override
    public List<CodeItemDTO> getCodeItemList(String groupId) {
        return codeMapper.selectCodeItemListByGroupId(groupId).stream()
                .map(i -> CodeItemDTO.builder()
                        .groupCode(i.getCodeGroupId())
                        .subCode(i.getCodeItemId())
                        .subName(i.getCodeItemName())
                        .desc(i.getDescription())
                        .order(i.getSortOrder())
                        .visible("Y".equals(i.getVisibleYn()))
                        .createdAt(i.getCreatedAt() != null ? i.getCreatedAt().toString() : null)
                        .updatedAt(i.getUpdatedAt() != null ? i.getUpdatedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public CodeItemDTO getCodeItem(String groupId, String itemId) {
        CodeItemDTO dto = codeMapper.selectCodeItemById(groupId, itemId);
        if (dto == null) throw new RuntimeException("해당 상세 코드를 찾을 수 없습니다.");

        return dto;
    }

    @Override
    @Transactional
    public void modifyItem(CodeItemDTO dto) {

        CodeItemDTO existing = codeMapper.selectCodeItemById(dto.getGroupCode(), dto.getSubCode());
        
        if (existing == null) throw new RuntimeException("수정하려는 상세코드가 존재하지 않습니다.");

        // 상세코드명이 변경되었을 때만 중복 체크
        if (!existing.getSubName().equals(dto.getSubName())) {
            if (codeMapper.countSubName(dto.getGroupCode(), dto.getSubName()) > 0) {
                throw new RuntimeException("해당 그룹 내에 이미 존재하는 상세코드명입니다.");
            }
        }

        CodeItem item = CodeItem.builder()
                .codeItemId(dto.getSubCode())
                .codeGroupId(dto.getGroupCode())
                .codeItemName(dto.getSubName())
                .description(dto.getDesc())
                .sortOrder(dto.getOrder())
                .visibleYn(dto.isVisible() ? "Y" : "N")
                .build();

        codeMapper.updateCodeItem(item);
    }

    /* 중복 체크 */
    @Override public boolean isGroupCodeDuplicate(String code) { return codeMapper.countGroupCode(code) > 0; }
    @Override public boolean isGroupNameDuplicate(String name) { return codeMapper.countGroupName(name) > 0; }
    @Override public boolean isSubCodeDuplicate(String groupCode, String subCode) { return codeMapper.countSubCode(groupCode, subCode) > 0; }
    @Override public boolean isSubNameDuplicate(String groupCode, String subName) { return codeMapper.countSubName(groupCode, subName) > 0; }
    
    @Override
    @Transactional
    public void registerItem(CodeItemDTO dto) {
        if (codeMapper.selectCodeGroupById(dto.getGroupCode()) == null) throw new RuntimeException("존재하지 않는 상위 그룹 코드입니다.");
        if (codeMapper.countSubCode(dto.getGroupCode(), dto.getSubCode()) > 0) throw new RuntimeException("중복 코드 ID");
        if (codeMapper.countSubName(dto.getGroupCode(), dto.getSubName()) > 0) throw new RuntimeException("중복 코드명");

        if (dto.getOrder() == null || dto.getOrder() == 0) {
            int maxOrder = codeMapper.selectMaxItemOrder(dto.getGroupCode());
            dto.setOrder(maxOrder + 1);
        }

        CodeItem item = CodeItem.builder()
                .codeItemId(dto.getSubCode())
                .codeGroupId(dto.getGroupCode())
                .codeItemName(dto.getSubName())
                .description(dto.getDesc())
                .sortOrder(dto.getOrder())
                .visibleYn(dto.isVisible() ? "Y" : "N")
                .build();
        codeMapper.insertCodeItem(item);
    }

    @Override
    @Transactional
    public void removeItem(String groupId, String itemId) {
        codeMapper.deleteCodeItem(groupId, itemId);
    }
}