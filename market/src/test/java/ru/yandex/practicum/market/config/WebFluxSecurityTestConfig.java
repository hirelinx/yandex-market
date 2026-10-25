package ru.yandex.practicum.market.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.market.repository.UserRepository;
import ru.yandex.practicum.market.security.SecurityConfig;

@TestConfiguration
@Import(SecurityConfig.class)
public class WebFluxSecurityTestConfig {
    @Bean
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }
}
