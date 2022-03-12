package mingu.bookreactive.ch9oauth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {

    @GetMapping
    public Mono<Rendering> home(
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        return Mono.just(Rendering.view("home.html")
                .modelAttribute("userName", oAuth2User.getName())
                .modelAttribute("authorities", oAuth2User.getAuthorities())
                .modelAttribute("clientName", authorizedClient.getClientRegistration().getClientName())
                .modelAttribute("userAttributes", oAuth2User.getAttributes())
                .build());
    }
}
