package jbell.safetypolicy.mapper;

import jbell.safetypolicy.dto.SafetyPolicyDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SafetyPolicyMapper {

    List<SafetyPolicyDTO> selectSafetyPolicyList(
        @Param("offset") int offset, 
        @Param("limit") int limit, 
        @Param("keyword") String keyword, 
        @Param("visibleYn") String visibleYn
    );

    long countSafetyPolicyList(
        @Param("keyword") String keyword, 
        @Param("visibleYn") String visibleYn
    );

    SafetyPolicyDTO selectSafetyPolicyDetail(@Param("contentId") Long contentId);

    int insertSafetyPolicy(SafetyPolicyDTO safetyPolicyDTO);

    int updateSafetyPolicy(SafetyPolicyDTO safetyPolicyDTO);
}