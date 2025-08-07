package com.loopers.domain.user;


import java.time.LocalDate;

public record UserRegisterResult(
        String id,
        LocalDate birthDate,
        String email
) {
    public static UserRegisterResult of(String id, LocalDate birthDate, String email) {
        return new UserRegisterResult(
                id,
                birthDate,
                email
        );
    }
}
