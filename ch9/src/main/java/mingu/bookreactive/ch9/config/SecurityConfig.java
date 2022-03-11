package mingu.bookreactive.ch9.config;

import mingu.bookreactive.ch9.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

@Configuration
//@EnableReactiveMethodSecurity // 메소드 수준 보안 테스트
public class SecurityConfig {

    public static final String USER = "USER";
    public static final String INVENTORY = "INVENTORY";

    @Bean
    public SecurityWebFilterChain customSecurityPolicy(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/").hasRole(INVENTORY)
                        .pathMatchers(HttpMethod.DELETE, "/**").hasRole(INVENTORY)
                        .anyExchange().authenticated()
                        .and()
                        .httpBasic()
                        .and()
                        .formLogin())
                .csrf().disable()
                .build();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository repository) {
        // FIXME User.withDefaultPasswordEncoder() is considered unsafe for production and is only intended for sample applications.
        return username -> repository.findByName(username)
                .map(user -> User.withDefaultPasswordEncoder()
                        .username(user.getName())
                        .password(user.getPassword())
                        .authorities(user.getRoles().toArray(new String[0]))
                        .build());
    }

    private static String role(String auth) {
        return "ROLE_" + auth;
    }

    @Bean
    public CommandLineRunner userLoader(ReactiveMongoOperations operations) {
        return args -> {
            operations.remove(new Query(), "user").block();
            operations.save(new mingu.bookreactive.ch9.entity.User("user", "user", List.of(role(USER)))).block();
            operations.save(new mingu.bookreactive.ch9.entity.User("admin", "admin", List.of(role(USER), role(INVENTORY)))).block();
        };
    }
}
