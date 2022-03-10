package mingu.bookreactive.ch8server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Controller
public class RSocketService {

    private static final Logger log = LoggerFactory.getLogger(RSocketService.class);

    private final ItemRepository repository;

    private final Sinks.Many<Item> itemsSink;

    public RSocketService(ItemRepository repository) {
        this.repository = repository;
        this.itemsSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @MessageMapping("newItems.request-response")
    public Mono<Item> processNewItemsViaRSocketRequestResponse(Item item) {
        log.info("newItems.request-response");
        return this.repository.save(item).doOnNext(this.itemsSink::tryEmitNext);
    }

    @MessageMapping("newItems.request-stream")
    public Flux<Item> findItemsViaRsocketRequestStream() {
        log.info("newItems.request-stream");
        return this.repository.findAll().doOnNext(this.itemsSink::tryEmitNext);
    }

    @MessageMapping("newItems.fire-and-forget")
    public Mono<Void> processNewItemsViaRSocketFireAndForgot(Item item) {
        log.info("newItems.fire-and-forget");
        return this.repository.save(item).doOnNext(this.itemsSink::tryEmitNext).then();
    }

    @MessageMapping("newItems.monitor")
    public Flux<Item> monitorNewItems() {
        log.info("newItems.monitor");
        return this.itemsSink.asFlux();
    }
}
