package com.login.webflux.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ServerWebExchange;

import java.io.*;
import java.util.Base64;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtils {
    public static Optional<HttpCookie> getCookie(ServerWebExchange exchange, String name) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(name);
        return Optional.ofNullable(cookie);
    }

    public static void addCookie(ServerWebExchange exchange, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .maxAge(maxAge)
                .sameSite("Lax") // Protects against CSRF while allowing OAuth redirect callback
                // .secure(true) // Enable in production with HTTPS
                .build();
        exchange.getResponse().addCookie(cookie);
    }

    public static void deleteCookie(ServerWebExchange exchange, String name) {
        ResponseCookie deleteCookie = ResponseCookie.from(name, "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
        exchange.getResponse().addCookie(deleteCookie);
    }

    public static String serialize(Object object) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            return Base64.getUrlEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to serialize object", e);
        }
    }

    public static <T> T deserialize(HttpCookie cookie, Class<T> cls) {
        byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return cls.cast(ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to deserialize object", e);
        }
    }
}
