package jbell.common.response;

import java.util.List;

public record PageResponse<T>(
    List<T> list,
    long total,
    int page,
    int size
) {
    // 필요시 전체 페이지 수 계산 메서드 추가 가능
    public int getTotalPages() {
        return (int) Math.ceil((double) total / size);
    }
}