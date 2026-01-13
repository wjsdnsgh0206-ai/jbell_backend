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

        // 2. DB 저장 (암호화 없이 request에 담긴 userPw 그대로 저장)
        userMapper.insertUser(request);
    }
    
    
    @Override
    @Transactional(readOnly = true)
    public boolean isUserIdDuplicated(String userId) {
        // Mapper에서 개수를 조회하여 0보다 크면 중복으로 판단
        return userMapper.existsByUserId(userId);
    }
}