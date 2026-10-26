package ru.yandex.practicum.market.web.advice;

import org.springframework.security.web.reactive.result.view.CsrfRequestDataValueProcessor;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class CsrfControllerAdvice {

    @ModelAttribute
    public Mono<CsrfToken> csrfToken(ServerWebExchange exchange) {
        Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
        if (csrfToken == null) {
            return Mono.empty();
        }

        return csrfToken.doOnNext(token ->
                exchange.getAttributes().put(
                        CsrfRequestDataValueProcessor.DEFAULT_CSRF_ATTR_NAME,
                        token
                )
        );
    }
}