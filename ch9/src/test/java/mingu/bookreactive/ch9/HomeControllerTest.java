package mingu.bookreactive.ch9;

import mingu.bookreactive.ch9.entity.Item;
import mingu.bookreactive.ch9.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
public class HomeControllerTest {
    @Autowired
    public WebTestClient webTestClient;

    @Autowired
    public ItemRepository repository;

    @Test
    @WithMockUser(username = "test", roles = { "SOME_OTHER_ROLE" })
    void addingInventoryWithRoleFails() {
        this.webTestClient.post().uri("/")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "test", roles = { "INVENTORY" })
    void addingInventoryWithProperRoleSucceeds() {
        this.webTestClient
                .post().uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{" +
                        "\"name\": \"iPhone 11\", " +
                        "\"description\": \"upgrade\", " +
                        "\"price\": 999.99" +
                        "}")
                .exchange()
                .expectStatus().isOk();

        this.repository.findByName("iPhone 11")
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getDescription()).isEqualTo("upgrade");
                    assertThat(item.getPrice()).isEqualTo(999.99);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @WithMockUser(username = "test", roles = { "SOME_OTHER_ROLE" })
    void deletingInventoryWithoutProperRoleFails() {
        this.webTestClient.delete().uri("/some-item").exchange().expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "dan", roles = { "INVENTORY" })
    void deletingInventoryWithProperRoleSucceeds() {
        String id = this.repository.findByName("iPhone 11").map(Item::getId).block();
        this.webTestClient.delete().uri("/" + id).exchange().expectStatus().isOk();
        this.repository.findByName("iPhone 11").as(StepVerifier::create).expectNextCount(0).verifyComplete();
    }

    // 메소드 수준 보안 테스트
    @Test
    @WithMockUser(username = "test", roles = { "SOME_OTHER_ROLE" })
    void ApiAddingInventoryWithoutProperRoleFails() {
        this.webTestClient
                .post().uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{" +
                        "\"name\": \"iPhone X\", " +
                        "\"description\": \"upgrade\", " +
                        "\"price\": 999.99" +
                        "}")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "test", roles = { "INVENTORY" })
    void ApiAddingInventoryWithProperRoleSucceeds() {
        this.repository.deleteAllByName("iPhone X").block();

        this.webTestClient
                .post().uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{" +
                        "\"name\": \"iPhone X\", " +
                        "\"description\": \"upgrade\", " +
                        "\"price\": 999.99" +
                        "}")
                .exchange()
                .expectStatus().isCreated();

        this.repository.findByName("iPhone X")
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getDescription()).isEqualTo("upgrade");
                    assertThat(item.getPrice()).isEqualTo(999.99);
                    return true;
                })
                .verifyComplete();
    }
}
