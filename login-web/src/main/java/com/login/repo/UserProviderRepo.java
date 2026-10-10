package com.login.repo;

import com.login.entities.UserProvider;
import com.login.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProviderRepo extends JpaRepository<UserProvider, Integer> {
    UserProvider findByProviderIdAndProvider(String providerId, AuthProvider provider);
    List<UserProvider> findByUserId(Integer userId);
}
