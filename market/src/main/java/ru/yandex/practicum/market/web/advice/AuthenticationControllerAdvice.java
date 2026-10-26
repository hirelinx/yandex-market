package ru.yandex.practicum.market.web.advice;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class AuthenticationControllerAdvice {

    @ModelAttribute("authenticated")
    public Mono<Boolean> authenticated() {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .map(authentication -> authentication.isAuthenticated()
                        && !(authentication instanceof AnonymousAuthenticationToken))
                .defaultIfEmpty(false);
    }
}
