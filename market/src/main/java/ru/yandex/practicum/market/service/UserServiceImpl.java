package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.model.User;
import ru.yandex.practicum.market.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Mono<User> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(authentication -> !(authentication instanceof AnonymousAuthenticationToken))
                .map(Authentication::getName)
                .defaultIfEmpty("")
                .flatMap(userRepository::findByUsername);
    }
}
