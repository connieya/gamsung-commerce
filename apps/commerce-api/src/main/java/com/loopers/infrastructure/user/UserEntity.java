package com.loopers.infrastructure.user;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.User;
import com.loopers.domain.user.vo.Gender;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {


    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    private String email;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;


    public static UserEntity fromDomain(User user) {
        UserEntity userEntity = new UserEntity();

        userEntity.userId = user.getUserId();
        userEntity.email = user.getEmail();
        userEntity.birthDate = user.getBirthDate().getBirthDate();
        userEntity.gender = user.getGender();

        return userEntity;
    }

    public User toDomain() {
        return User
                .builder()
                .id(id)
                .userId(userId)
                .email(email)
                .birthDate(new BirthDate(birthDate.toString()))
                .gender(gender)
                .build();
    }
}
