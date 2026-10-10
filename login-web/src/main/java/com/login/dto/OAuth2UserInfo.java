package com.login.dto;

import com.login.enums.AuthProvider;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class OAuth2UserInfo {
    private AuthProvider authProvider;
    private String providerId;
    private String email;
    private String name;
}
