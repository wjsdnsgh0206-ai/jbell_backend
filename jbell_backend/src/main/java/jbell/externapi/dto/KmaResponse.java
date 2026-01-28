package jbell.externapi.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class KmaResponse<T> {
    private String resultCode;
    private String resultMsg;
    private List<T> items;
}