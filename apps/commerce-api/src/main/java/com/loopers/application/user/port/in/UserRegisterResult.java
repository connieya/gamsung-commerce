package com.loopers.application.user.port.in;


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
