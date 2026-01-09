package com.jbell.jbellBackend.user.controller;

import com.jbell.jbellBackend.user.dto.UserDTO; 
import com.jbell.jbellBackend.user.entity.User;
import com.jbell.jbellBackend.user.repository.UserRepository;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // 리액트에서의 접속을 허용합니다.
public class UserController {

    private final UserRepository userRepository;

    // 생성자 주입: 스프링이 자동으로 UserRepository(일꾼)를 연결해줍니다.
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<User> getUsers(UserDTO request) { 
        // 1. 이름 검색 조건이 있는 경우
        if (request.getName() != null && !request.getName().isEmpty()) {
            return userRepository.findByNameContaining(request.getName());
        }

        // 2. 지역 검색 조건이 있는 경우
        if (request.getResidenceArea() != null && !request.getResidenceArea().isEmpty()) {
            return userRepository.findByResidenceAreaContaining(request.getResidenceArea());
        }

        // 3. 검색 조건이 없으면 DB에서 모든 유저 정보를 가져옵니다.
        return userRepository.findAll();
    }
}