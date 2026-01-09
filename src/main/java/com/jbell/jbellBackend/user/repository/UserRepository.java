package com.jbell.jbellBackend.user.repository; // 언더바 제거된 경로

import com.jbell.jbellBackend.user.entity.User; // Entity 경로 수정 확인!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
// 여기서 JPA 실행


    // 이름 포함 검색 (LIKE %name%)
    List<User> findByNameContaining(String name);

    // 지역 포함 검색 (LIKE %area%)
    List<User> findByResidenceAreaContaining(String area);
}