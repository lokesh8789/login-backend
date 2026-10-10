package com.login.entities;

import com.login.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_providers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userId;
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    @Column(name = "provider_id")
    private String providerId;
}
