package com.loopers.domain.user;

import com.loopers.domain.common.SelfValidating;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Getter
public class BirthDate extends SelfValidating<BirthDate> {

    @NotNull(message = "생년월일은 필수입니다.")
    private final LocalDate birthDate;

    public BirthDate(String dateString) {
        try{

            this.birthDate = LocalDate.parse(dateString);
        }catch (DateTimeParseException e) {
            throw new IllegalArgumentException("생년월일은 yyyy-MM-dd 형식이어야 합니다. (예: 1990-01-01)");
        }

        this.validateSelf();
    }
}
