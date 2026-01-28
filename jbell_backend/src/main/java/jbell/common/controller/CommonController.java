package jbell.common.controller;

import jbell.common.mapper.CommonMapper;
import jbell.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    private final CommonMapper commonMapper;

    // 생성자를 통해 CommonMapper를 반드시 주입받아야 합니다.
    public CommonController(CommonMapper commonMapper) {
        this.commonMapper = commonMapper;
    }

    @GetMapping("/codes/{groupId}")
    public Mono<ApiResponse<List<Map<String, String>>>> getCodes(@PathVariable("groupId") String groupId) {
        return Mono.fromCallable(() -> {
            List<Map<String, String>> codes = commonMapper.getCodeListByGroupId(groupId);
            return ApiResponse.success(codes);
        });
    }
}