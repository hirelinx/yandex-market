package ru.yandex.practicum.market.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.HttpStatusReturningServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.authentication.ServerHttpBasicAuthenticationConverter;
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler;
import ru.yandex.practicum.market.repository.UserRepository;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http
//                                                         ,ReactiveAuthenticationManager authenticationManager
    ) {
//        AuthenticationWebFilter authFilter = new AuthenticationWebFilter(authenticationManager);
//        authFilter.setServerAuthenticationConverter(new ServerHttpBasicAuthenticationConverter());

        DelegatingServerLogoutHandler logoutHandler = new DelegatingServerLogoutHandler(
                new SecurityContextServerLogoutHandler(),
                new WebSessionServerLogoutHandler()
        );

        XorServerCsrfTokenRequestAttributeHandler csrfHandler = new XorServerCsrfTokenRequestAttributeHandler();
        csrfHandler.setTokenFromMultipartDataEnabled(true);

        return http
                .formLogin(withDefaults())
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
//                .addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .logout(logout -> logout
                        .logoutHandler(logoutHandler)
                        .logoutSuccessHandler(new HttpStatusReturningServerLogoutSuccessHandler())

                )
                .csrf(csrfSpec ->
                        csrfSpec.csrfTokenRequestHandler(csrfHandler))
                .authorizeExchange(exchanges ->
                        exchanges
                                .pathMatchers(HttpMethod.GET, "/", "/items/**", "/files/**").permitAll()
                                .anyExchange().authenticated())

                .build();
    }

//    @Bean
//    public ReactiveAuthenticationManager reactiveAuthenticationManager(
//            ReactiveUserDetailsService userDetailsService,
//            PasswordEncoder passwordEncoder
//    ) {
//        UserDetailsRepositoryReactiveAuthenticationManager manager =
//                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
//
//        manager.setPasswordEncoder(passwordEncoder);
//        return manager;
//
//    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
        return (username) -> {
            var user = userRepository.findByUsername(username);

            return user.map(u -> new User(
                    u.getUsername(),
                    u.getPasswordHash(),
                    List.of()
            ));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
