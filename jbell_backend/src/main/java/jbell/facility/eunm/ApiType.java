package jbell.facility.eunm;

public enum ApiType {
    // 순서: apiId, nameField, addrField, latField, lonField, areaField, capacityField
    SHELTER_IMSI("DSSP-IF-10945", "VT_ACMDFCLTY_NM", "RN_DTL_ADDR", "LA", "LO", "FCLTY_AR", "VT_ACMD_PSBL_NMPR"),
    SHELTER_HEAT("DSSP-IF-10942", "RSTR_NM", "RN_DTL_ADRES", "LA", "LO", "AR", "USE_PSBL_NMPR"),
    SHELTER_COLD("DSSP-IF-10804", "REARE_NM", "RONA_DADDR", "LAT", "LOT", "NON_DATE", "UTZTN_PSBLTY_TNOP"),
    SHELTER_EARTHQUAKE("DSSP-IF-10943", "VT_ACMDFCLTY_NM", "RN_DTL_ADRES", "LA", "LO", "FCLTY_AR", "VT_ACMD_PSBL_NMPR"),
    SHELTER_NUCLEAR("DSSP-IF-10417", "FCLT_NM", "ROAD_NM_ADDR", "DMS", "DMS", "NON_DATE", "SHNT_PSBLTY_NOPE"),
    SHELTER_CIVIL("DSSP-IF-00195", "FCLT_NM", "FCLT_ADDR_RONA", "DMS", "DMS", "FCLT_SCL", "SHNT_PSBLTY_NOPE");

	public final String apiId;
    public final String nameField;
    public final String addrField;
    public final String latField;
    public final String lonField;
    public final String areaField;     // 추가
    public final String capacityField; // 추가

    ApiType(String apiId, String name, String addr, String lat, String lon, String area, String capacity) {
        this.apiId = apiId;
        this.nameField = name;
        this.addrField = addr;
        this.latField = lat;
        this.lonField = lon;
        this.areaField = area;
        this.capacityField = capacity;
    }
}
