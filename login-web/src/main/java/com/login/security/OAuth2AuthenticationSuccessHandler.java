package com.login.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.login.dto.OAuth2UserInfo;
import com.login.dto.UserDto;
import com.login.enums.AuthProvider;
import com.login.enums.ErrorCode;
import com.login.exceptions.StrategyNotFoundException;
import com.login.factory.OAuth2UserStrategyFactory;
import com.login.service.UserService;
import com.login.utils.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtTokenHelper jwtTokenHelper;
    private final ObjectMapper objectMapper;
    private final OAuth2UserStrategyFactory oAuth2UserStrategyFactory;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;

            OAuth2UserInfo userInfo = oAuth2UserStrategyFactory.getStrategy(authenticationToken.getAuthorizedClientRegistrationId())
                    .extract(authenticationToken);

            if (userInfo.getProviderId() == null || userInfo.getProviderId().isBlank()) {
                redirectWithError(response, ErrorCode.INVALID_PROVIDER_RESPONSE.getValue());
                return;
            }

            if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
                redirectWithError(response, ErrorCode.EMAIL_NOT_PROVIDED.getValue());
                return;
            }

            UserDto user = userService.processOAuthUser(userInfo);
            String token = jwtTokenHelper.generateToken(user.getEmail(), user.getId().toString());
            Date expiration = jwtTokenHelper.getExpirationDateFromToken(token);
            String data = objectMapper.writeValueAsString(new JwtAuthResponse(user, token, new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(expiration)));
            String encodedData = Base64.getUrlEncoder().encodeToString(data.getBytes());

            response.sendRedirect(Constants.FRONTEND_OAUTH_SUCCESS_URL + "?data=" + encodedData);
        } catch (StrategyNotFoundException e) {
            log.error("Strategy Exception while handling oauth2 success ", e);
            redirectWithError(response, ErrorCode.UNSUPPORTED_PROVIDER.getValue());
        } catch (Exception e) {
            log.error("Exception Occurred while handling oauth2 success ", e);
            redirectWithError(response, ErrorCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    private void redirectWithError(HttpServletResponse response, String error) throws IOException {
        response.sendRedirect(Constants.FRONTEND_OAUTH_FAILURE_URL + "?error=" + error);
    }
}
