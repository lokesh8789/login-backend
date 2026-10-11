package com.login.webflux.entity;

import com.login.webflux.enums.AuthProvider;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "user_providers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserProvider {
    @Id
    private Integer id;
    @Column("user_id")
    private Integer userId;
    private AuthProvider provider;
    @Column("provider_id")
    private String providerId;
}
