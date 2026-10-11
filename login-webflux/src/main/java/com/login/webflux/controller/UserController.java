package com.login.webflux.controller;

import com.login.webflux.dto.UserDto;
import com.login.webflux.service.UserService;
import com.login.webflux.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/")
    public Flux<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping
    public Flux<UserDto> getByNameKeyword(@RequestParam String name) {
        log.info("name:{}",name);
        return userService.getUsersWithNameContains(name);
    }

    @GetMapping("/{id}")
    public Mono<UserDto> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResponse>> registerUser(@RequestBody UserDto userDto) {
        return userService.registerUser(userDto)
                .filter(Boolean.TRUE::equals)
                .map(o -> new ResponseEntity<>(new ApiResponse("User Registered Successfully", true), HttpStatus.CREATED))
                .defaultIfEmpty(new ResponseEntity<>(new ApiResponse("Something Went Wrong", false), HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
