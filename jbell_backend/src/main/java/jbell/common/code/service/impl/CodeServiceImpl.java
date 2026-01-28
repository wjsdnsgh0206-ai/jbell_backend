package jbell.common.code.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
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
        // XML에서 IF(visible_yn='Y', 1, 0) 처리를 했으므로 그대로 반환하면 DTO의 boolean visible 필드에 자동 매핑됩니다.
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
    public void registerGroup(CodeGroupDTO dto) {
        if (codeMapper.countGroupCode(dto.getGroupCode()) > 0) throw new RuntimeException("이미 존재하는 그룹코드입니다.");
        if (codeMapper.countGroupName(dto.getGroupName()) > 0) throw new RuntimeException("이미 존재하는 그룹명입니다.");

        CodeGroup group = CodeGroup.builder()
                .codeGroupId(dto.getGroupCode())
                .codeGroupName(dto.getGroupName())
                .codeDesc(dto.getDesc())
                .sortOrder(dto.getOrder())
                .visibleYn(dto.isVisible() ? "Y" : "N") // DB 저장은 Y/N으로
                .build();
                
        codeMapper.insertCodeGroup(group);
    }

    @Override
    public void modifyGroup(CodeGroupDTO dto) {
        CodeGroup existing = codeMapper.selectCodeGroupById(dto.getGroupCode());
        if (existing == null) throw new RuntimeException("수정하려는 그룹코드가 존재하지 않습니다.");

        if (!existing.getCodeGroupName().equals(dto.getGroupName())) {
            if (codeMapper.countGroupName(dto.getGroupName()) > 0) throw new RuntimeException("이미 사용 중인 그룹명입니다.");
        }

        CodeGroup group = CodeGroup.builder()
                .codeGroupId(dto.getGroupCode())
                .codeGroupName(dto.getGroupName())
                .codeDesc(dto.getDesc())
                .sortOrder(dto.getOrder())
                .visibleYn(dto.isVisible() ? "Y" : "N")
                .build();

        codeMapper.updateCodeGroup(group);
    }

    @Override
    public void removeGroup(String id) {
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
        // XML에서 IF 처리를 통해 DTO의 boolean visible 필드에 이미 true/false가 잘 들어온 상태입니다.
        return dto;
    }

    @Override
    public void modifyItem(CodeItemDTO dto) {
        // [수정 포인트] 이미 CodeItemDTO를 반환하도록 매퍼를 수정하셨으므로 그대로 사용합니다.
        CodeItemDTO existing = codeMapper.selectCodeItemById(dto.getGroupCode(), dto.getSubCode());
        
        if (existing == null) throw new RuntimeException("수정하려는 상세코드가 존재하지 않습니다.");

        // [중요] 상세코드명이 변경되었을 때만 중복 체크
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
                .visibleYn(dto.isVisible() ? "Y" : "N") // 화면의 boolean을 DB의 Y/N으로 변환
                .build();

        codeMapper.updateCodeItem(item);
    }

    /* 중복 체크 메서드들 */
    @Override public boolean isGroupCodeDuplicate(String code) { return codeMapper.countGroupCode(code) > 0; }
    @Override public boolean isGroupNameDuplicate(String name) { return codeMapper.countGroupName(name) > 0; }
    @Override public boolean isSubCodeDuplicate(String groupCode, String subCode) { return codeMapper.countSubCode(groupCode, subCode) > 0; }
    @Override public boolean isSubNameDuplicate(String groupCode, String subName) { return codeMapper.countSubName(groupCode, subName) > 0; }
    
    @Override
    public void registerItem(CodeItemDTO dto) {
        if (codeMapper.selectCodeGroupById(dto.getGroupCode()) == null) throw new RuntimeException("존재하지 않는 상위 그룹 코드입니다.");
        if (codeMapper.countSubCode(dto.getGroupCode(), dto.getSubCode()) > 0) throw new RuntimeException("중복 코드 ID");
        if (codeMapper.countSubName(dto.getGroupCode(), dto.getSubName()) > 0) throw new RuntimeException("중복 코드명");

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
    public void removeItem(String groupId, String itemId) {
        codeMapper.deleteCodeItem(groupId, itemId);
    }
}