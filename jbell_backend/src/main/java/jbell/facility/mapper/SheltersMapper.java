package jbell.facility.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import jbell.facility.dto.SheltersDTO;

@Mapper
public interface SheltersMapper {
    // static이 아닌 인스턴스 메서드로 정의
	void upsertShelters(@Param("list") List<SheltersDTO> list);
}
