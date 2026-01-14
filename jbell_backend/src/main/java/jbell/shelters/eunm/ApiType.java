package jbell.shelters.eunm;

public enum ApiType {
    SHELTER_IMSI("DSSP-IF-10945", "VT_ACMDFCLTY_NM", "RN_DTL_ADDR", "LA", "LO"), // 주소 필드 확인 필요
    SHELTER_HEAT("DSSP-IF-10942", "RSTR_NM", "RN_DTL_ADRES", "LA", "LO"),
    SHELTER_COLD("DSSP-IF-10804", "REARE_NM", "RONA_DADDR", "LAT", "LOT"),
    SHELTER_EARTHQUAKE("DSSP-IF-10943", "VT_ACMDFCLTY_NM", "RN_DTL_ADRES", "LA", "LO"),
    SHELTER_NUCLEAR("DSSP-IF-10417", "FCLT_NM", "ROAD_NM_ADDR", "DMS", "DMS"), // 도분초
    SHELTER_CIVIL("DSSP-IF-00195", "FCLT_NM", "FCLT_ADDR_RONA", "DMS", "DMS"); // 도분초

    public final String apiId;
    public final String nameField;
    public final String addrField;
    public final String latField;
    public final String lonField;

    ApiType(String apiId, String nameField, String addrField, String latField, String lonField) {
        this.apiId = apiId;
        this.nameField = nameField;
        this.addrField = addrField;
        this.latField = latField;
        this.lonField = lonField;
    }
}
