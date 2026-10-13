package ru.yandex.practicum.pay.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@Table(name = "accounts")
@RequiredArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @Column("id")
    private Integer id;

    @NonNull
    @Column("balance")
    private BigDecimal balance;
}
