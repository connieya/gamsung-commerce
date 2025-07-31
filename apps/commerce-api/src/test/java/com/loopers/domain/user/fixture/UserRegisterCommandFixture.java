package com.loopers.domain.user.fixture;

import com.loopers.domain.user.UserRegisterCommand;
import org.instancio.Instancio;
import org.instancio.InstancioApi;
import org.instancio.Select;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserRegisterCommandFixture {
    public static InstancioApi<UserRegisterCommand> complete() {
        return Instancio.of(UserRegisterCommand.class)
                .generate(Select.field(UserRegisterCommand::getUserId), gen -> gen.string()
                        .length(5, 10)
                        .alphaNumeric())
                .generate(Select.field(UserRegisterCommand::getEmail), gen -> gen.net().email())
                .generate(Select.field(UserRegisterCommand::getBirthDate), gen -> gen.temporal()
                        .localDate()
                        .range(LocalDate.of(1950, 1, 1), LocalDate.of(2005, 12, 31))
                        .as(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
    }

}
