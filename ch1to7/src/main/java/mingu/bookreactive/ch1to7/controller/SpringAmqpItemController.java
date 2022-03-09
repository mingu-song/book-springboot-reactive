package mingu.bookreactive.ch1to7.controller;

import mingu.bookreactive.ch1to7.entity.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;

@RestController
public class SpringAmqpItemController {
    private static final Logger log = LoggerFactory.getLogger(SpringAmqpItemController.class);

    private final AmqpTemplate template;

    public SpringAmqpItemController(AmqpTemplate template) {
        this.template = template;
    }

    @PostMapping("/items")
    public Mono<ResponseEntity<?>> addNewItemUsingSpringAmqp(@RequestBody Mono<Item> item) {
        return item.subscribeOn(Schedulers.boundedElastic())
                .flatMap(content -> Mono.fromCallable(() -> {
                    this.template.convertAndSend("test-spring-boot", "new-item-spring-amqp", content);
                    return ResponseEntity.created(URI.create("/items")).build();
                }));
    }
}
