package jbell.common.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommonMapper {
    // XML의 id와 메서드명이 일치해야 합니다.
    List<Map<String, String>> getCodeListByGroupId(@Param("groupId") String groupId);
}