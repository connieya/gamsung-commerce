package com.loopers.infrastructure.user.entity;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.BirthDate;
import com.loopers.domain.user.User;
import com.loopers.domain.user.vo.Gender;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "member")
public class UserEntity extends BaseEntity {


    private String userId;

    private String email;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;


    public static UserEntity fromDomain(User user) {
        UserEntity userEntity = new UserEntity();

        userEntity.userId = user.getId();
        userEntity.email = user.getEmail();
        userEntity.birthDate = user.getBirthDate().getBirthDate();
        userEntity.gender = user.getGender();

        return userEntity;
    }

    public User toDomain() {
        return User
                .builder()
                .id(userId)
                .email(email)
                .birthDate(new BirthDate(birthDate.toString()))
                .gender(gender)
                .build();
    }
}
