package ru.yandex.practicum.market.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(name = "users")
@ToString
public non-sealed class User implements IEntity {
    @Id
    @Column("id")
    private Long id;

    @Column("username")
    private String username;
    @Column("password_hash")
    private String passwordHash;
}
