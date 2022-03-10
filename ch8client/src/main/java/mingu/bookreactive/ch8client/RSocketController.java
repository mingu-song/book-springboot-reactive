package mingu.bookreactive.ch8client;

import io.rsocket.metadata.WellKnownMimeType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;

@RestController
public class RSocketController {

//    private final Mono<RSocketRequester> requester;
private final RSocketRequester requester;

    public RSocketController(RSocketRequester.Builder builder) {
        this.requester = builder
                .rsocketConnector(connector -> connector.reconnect(Retry.fixedDelay(5, Duration.ofSeconds(2))))
                .metadataMimeType(MediaType.parseMediaType(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.toString()))
                .tcp("localhost", 7000);
//        this.requester = builder.dataMimeType(MediaType.APPLICATION_JSON)
//                .metadataMimeType(MediaType.parseMediaType(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.toString()))
//                .connectTcp("localhost", 7000)
//                .retry(5)
//                .cache();
    }

    @PostMapping("/items/request-response")
    Mono<ResponseEntity<?>> addNewItemUsingRSocketRequestResponse(@RequestBody Item item) {
        return this.requester.route("newItems.request-response").data(item).retrieveMono(Item.class)
                .map(savedItem -> ResponseEntity.created(URI.create("/items/request-response")).body(savedItem));
//        return this.requester
//                .flatMap(rSocketRequester -> rSocketRequester
//                        .route("newItems.request-response")
//                        .data(item)
//                        .retrieveMono(Item.class))
//                .map(savedItem -> ResponseEntity.created(URI.create("/items/request-response")).body(savedItem));
    }

    @GetMapping(value = "/items/request-stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    Flux<Item> findItemsUsingRSocketRequestStream() {
        return this.requester.route("newItems.request-stream").retrieveFlux(Item.class).delayElements(Duration.ofSeconds(1));
//        return this.requester
//                .flatMapMany(rSocketRequester -> rSocketRequester
//                        .route("newItems.request-stream")
//                        .retrieveFlux(Item.class)
//                        .delayElements(Duration.ofSeconds(1)));
    }

    @PostMapping("/items/fire-and-forget")
    Mono<ResponseEntity<?>> addNewItemUsingRSocketFireAndForget(@RequestBody Item item) {
        return this.requester.route("newItems.fire-and-forget").data(item).send()
                .then(Mono.just(ResponseEntity.created(URI.create("/items/fire-and-forget")).build()));
//        return this.requester
//                .flatMap(rSocketRequester -> rSocketRequester
//                        .route("newItems.fire-and-forget")
//                        .data(item)
//                        .send())
//                .then(Mono.just(ResponseEntity.created(URI.create("/items/fire-and-forget")).build()));
    }

    @GetMapping(value = "/items", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Item> liveUpdates() {
        return this.requester.route("newItems.monitor").retrieveFlux(Item.class);
//        return this.requester
//                .flatMapMany(rSocketRequester -> rSocketRequester
//                        .route("newItems.monitor")
//                        .retrieveFlux(Item.class));
    }
}
