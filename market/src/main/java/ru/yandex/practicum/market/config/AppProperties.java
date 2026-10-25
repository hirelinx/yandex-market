package ru.yandex.practicum.market.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String uploadsDir,
        List<UserProperties> users,
        UserProperties sellerUser
) {
    public record UserProperties (
            String username,
            String passwordHash
    ) {}
}

