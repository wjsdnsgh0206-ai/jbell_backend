package jbell.facility.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;


import jbell.facility.dto.FacilityDTO;

@Mapper
public interface FacilityMapper {
	
	FacilityDTO getFacilityById(Long fcltId);
    void insertFacility(FacilityDTO dto);
    void updateFacility(FacilityDTO dto);
    void deleteFacilities(List<Long> ids);
	
	
	// 조건 검색 및 페이징 조회
    // 리스트 조회 (정렬 파라미터 추가)
	List<FacilityDTO> getFacilityList(
	    @Param("ctpvNm") String ctpvNm, 
	    @Param("sggNm") String sggNm, 
	    @Param("fcltNm") String fcltNm, 
	    @Param("roadNmAddr") String roadNmAddr,
	    @Param("offset") int offset, 
	    @Param("limit") int limit,
	    @Param("sortKey") String sortKey,
	    @Param("sortOrder") String sortOrder
	);
	
	// 전체개수 조회
	int getFacilityCount(
	    @Param("ctpvNm") String ctpvNm, 
	    @Param("sggNm") String sggNm, 
	    @Param("fcltNm") String fcltNm,
	    @Param("roadNmAddr") String roadNmAddr
	);
	
	
    // static이 아닌 인스턴스 메서드로 정의
	void upsertFacility(@Param("list") List<FacilityDTO> list);
}
