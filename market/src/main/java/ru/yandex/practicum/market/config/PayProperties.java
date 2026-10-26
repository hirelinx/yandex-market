package ru.yandex.practicum.market.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.pay")
public record PayProperties(
        String baseUrl
) {
}
