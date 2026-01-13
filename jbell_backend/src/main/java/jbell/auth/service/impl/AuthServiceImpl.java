package jbell.auth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jbell.auth.dto.SignupRequest;
import jbell.auth.mapper.UserMapper;
import jbell.auth.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void registerUser(SignupRequest request) {
        // 1. 아이디 중복 체크
        if (userMapper.existsByUserId(request.getUserId()) > 0) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        // 2. DB 저장 (암호화 없이 request에 담긴 userPw 그대로 저장)
        userMapper.insertUser(request);
    }
}