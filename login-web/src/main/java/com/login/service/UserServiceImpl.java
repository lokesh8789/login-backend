package com.login.service;

import com.login.dto.OAuth2UserInfo;
import com.login.dto.UserDto;
import com.login.entities.User;
import com.login.entities.UserProvider;
import com.login.enums.AuthProvider;
import com.login.exceptions.UserDoesNotExistException;
import com.login.exceptions.UserExistException;
import com.login.repo.UserProviderRepo;
import com.login.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    public ModelMapper modelMapper;
    @Autowired
    public UserRepo userRepo;
    @Autowired
    private UserProviderRepo userProviderRepo;

    @Override
    public boolean registerUser(UserDto userDto) {
        String email = userDto.getEmail();
        if (userRepo.findByEmail(email) != null) {
            throw new UserExistException("User with emailId- " + email + " already exist");
        }
        User user = modelMapper.map(userDto, User.class);
        try {
            userRepo.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepo.findAll();
        return users.stream().map(user -> modelMapper.map(user, UserDto.class)).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Integer id) {
        log.info("Fetching User for id: {}", id);
        User user = userRepo.findById(id).orElseThrow(() -> new UserDoesNotExistException("User does not exist"));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto findByEmail(String email) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new UserDoesNotExistException("User Does Not Exist");
        }
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public List<UserDto> findByNameContaining(String name) {
        List<User> users = userRepo.findByNameContaining(name);
        if (users == null || users.isEmpty()) {
            throw new UserDoesNotExistException("User Does Not Exist");
        }
        return users.stream().map(user -> modelMapper.map(user, UserDto.class)).collect(Collectors.toList());
    }

    @Override
    public UserDto deleteUser(Integer id) {
        User user = userRepo.findById(id).orElseThrow(() -> new UserDoesNotExistException("User Does Not Exist"));
        userRepo.delete(user);
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Integer id) {
        User user = userRepo.findById(id).orElseThrow(() -> new UserDoesNotExistException("User Does Not Exist"));
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        user.setPassword(userDto.getPassword());
        User save = userRepo.save(user);
        return modelMapper.map(save, UserDto.class);
    }

    @Override
    public UserProvider getUserProvider(String providerId, AuthProvider provider) {
        return userProviderRepo.findByProviderIdAndProvider(providerId, provider);
    }

    @Override
    public UserDto processOAuthUser(OAuth2UserInfo userInfo) {
        UserProvider userProvider = userProviderRepo.findByProviderIdAndProvider(userInfo.getProviderId(), userInfo.getAuthProvider());
        if (userProvider == null) {
            UserDto userDto;
            try {
                userDto = findByEmail(userInfo.getEmail());
            } catch (UserDoesNotExistException e) {
                log.info("User Not Found, So Creating New For Email: {}", userInfo.getEmail());
                String displayName = (userInfo.getName() != null && !userInfo.getName().isBlank())
                        ? userInfo.getName()
                        : userInfo.getEmail().substring(0, userInfo.getEmail().indexOf('@'));
                User user = User.builder()
                        .name(displayName)
                        .email(userInfo.getEmail())
                        .build();
                user = userRepo.save(user);
                userDto = modelMapper.map(user, UserDto.class);
            }
            UserProvider newProvider = UserProvider.builder()
                    .providerId(userInfo.getProviderId())
                    .provider(userInfo.getAuthProvider())
                    .userId(userDto.getId())
                    .build();
            log.info("Creating New Provider {} For Email: {}", userInfo.getAuthProvider(), userInfo.getEmail());
            userProviderRepo.save(newProvider);
            return userDto;
        }
        return getUserById(userProvider.getUserId());
    }
}
