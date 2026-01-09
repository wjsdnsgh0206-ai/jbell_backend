package com.jbell.jbellBackend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "user") // 테이블명이 'user'
@Getter @Setter
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "user_id")
    private String userId; // 'admin', 'user01' 등

    @Column(name = "password_hash")
    private String passwordHash;

    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate; // 1980-01-01 형태 매핑

    private String email;

    private Integer status; // 1

    @Column(name = "user_grade")
    private String userGrade; // 'A', 'U'

    @Column(name = "residence_area")
    private String residenceArea; // '서울특별시 종로구'

    @Column(name = "code_item_id")
    private String codeItemId; // 'G001'

    @Column(name = "code_group_id")
    private String codeGroupId; // 'USER_GRADE'
}