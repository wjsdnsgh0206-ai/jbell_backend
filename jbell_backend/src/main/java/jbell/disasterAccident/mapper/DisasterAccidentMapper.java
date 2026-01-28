package jbell.disasterAccident.mapper;

import jbell.disasterAccident.dto.DisasterAccidentDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DisasterAccidentMapper {
    int insertEarthquake(DisasterAccidentDTO dto);
    int insertTyphoonInfo(DisasterAccidentDTO dto);
    int insertTyphoonTrack(DisasterAccidentDTO dto);
    int insertLandslide(DisasterAccidentDTO dto);
    void insertForestFire(DisasterAccidentDTO dto);
}