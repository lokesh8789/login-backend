package com.login.webflux.service;

import com.login.webflux.dto.OAuth2UserInfo;
import com.login.webflux.dto.UserDto;
import com.login.webflux.entity.User;
import com.login.webflux.entity.UserProvider;
import com.login.webflux.exceptions.UserExistException;
import com.login.webflux.repo.UserProviderRepo;
import com.login.webflux.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final UserProviderRepo userProviderRepo;
    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Mono<Boolean> registerUser(UserDto userDto) {
        return userRepo.findByEmail(userDto.getEmail())
                .flatMap(user -> Mono.<Boolean>error(() -> new UserExistException("User with emailId- " + user.getEmail() + " already exist")))
                .switchIfEmpty(Mono.defer(() -> {
                    User user = modelMapper.map(userDto, User.class);
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    return userRepo.save(user)
                            .thenReturn(true);
                }))
                .onErrorResume(e -> {
                    if (e instanceof UserExistException) {
                        return Mono.error(e);
                    }
                    return Mono.just(false);
                });
    }

    public Mono<UserDto> getUserById(int id) {
        return userRepo.findById(id)
                .map(user -> modelMapper.map(user, UserDto.class));
    }

    public Mono<UserDto> getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .map(user -> modelMapper.map(user, UserDto.class));
    }

    public Flux<UserDto> getAllUsers() {
        return userRepo.findAll()
                .map(user -> modelMapper.map(user, UserDto.class));
    }

    public Flux<UserDto> getUsersWithNameContains(String name) {
        return userRepo.findByNameContaining(name)
                .map(user -> modelMapper.map(user, UserDto.class));
    }

    public Mono<UserDto> processOAuthUser(OAuth2UserInfo userInfo) {
        return userProviderRepo.findByProviderIdAndProvider(userInfo.getProviderId(), userInfo.getAuthProvider())
                .map(userProvider -> getUserById(userProvider.getUserId()))
                .switchIfEmpty(Mono.defer(() -> getUserByEmail(userInfo.getEmail())
                        .switchIfEmpty(Mono.defer(() -> {
                                    String email = userInfo.getEmail();
                                    String displayName = (userInfo.getName() != null && !userInfo.getName().isBlank())
                                            ? userInfo.getName()
                                            : (email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : "User");
                                    User u = User.builder()
                                            .name(displayName)
                                            .email(userInfo.getEmail())
                                            .build();
                                    return userRepo.save(u)
                                            .map(user -> modelMapper.map(user, UserDto.class));
                                })
                        )
                        .flatMap(userDto -> {
                            UserProvider newProvider = UserProvider.builder()
                                    .providerId(userInfo.getProviderId())
                                    .provider(userInfo.getAuthProvider())
                                    .userId(userDto.getId())
                                    .build();
                            log.info("Creating New Provider {} For Email: {}", userInfo.getAuthProvider(), userInfo.getEmail());
                            return userProviderRepo.save(newProvider)
                                    .thenReturn(userDto);
                        })
                        .map(Mono::just))
                )
                .flatMap(mono -> mono);
    }

    /*public Mono<UserDto> processOAuthUser(OAuth2UserInfo userInfo) {
        return userProviderRepo.findByProviderIdAndProvider(userInfo.getProviderId(), userInfo.getAuthProvider())
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(opt -> {
                    if (opt.isPresent()) {
                        return getUserById(opt.get().getUserId());
                    }
                    return getUserByEmail(userInfo.getEmail())
                            .switchIfEmpty(Mono.defer(() -> {
                                    String email = userInfo.getEmail();
                                    String displayName = (userInfo.getName() != null && !userInfo.getName().isBlank())
                                            ? userInfo.getName()
                                            : (email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : "User");
                                    User u = User.builder()
                                            .name(displayName)
                                            .email(userInfo.getEmail())
                                            .build();
                                    return userRepo.save(u)
                                            .map(user -> modelMapper.map(user, UserDto.class));
                                })
                        )
                            .flatMap(userDto -> {
                                UserProvider newProvider = UserProvider.builder()
                                        .providerId(userInfo.getProviderId())
                                        .provider(userInfo.getAuthProvider())
                                        .userId(userDto.getId())
                                        .build();
                                log.info("Creating New Provider {} For Email: {}", userInfo.getAuthProvider(), userInfo.getEmail());
                                return userProviderRepo.save(newProvider)
                                        .thenReturn(userDto);
                            });
                });
    }*/
}
