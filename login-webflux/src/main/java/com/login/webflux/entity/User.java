package com.login.webflux.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class User {

    @Id
    @Column("id")
    private Integer id;

    @Column("name")
    private String name;

    @Column("email")
    private String email;

    @Column("password")
    private String password;
}
