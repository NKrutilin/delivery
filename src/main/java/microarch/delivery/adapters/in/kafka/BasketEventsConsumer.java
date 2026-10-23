package microarch.delivery.adapters.in.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import microarch.delivery.core.application.command.CreateOrderCommand;
import microarch.delivery.core.application.command.handler.CreateOrderHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import queues.basket.events.BasketEventsProto;

import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class BasketEventsConsumer {

    public static final String TOPIC = "basket.events";

    private final CreateOrderHandler createOrderHandler;

    @KafkaListener(topics = TOPIC)
    public void handle(final byte[] payload) {
        val event = parse(payload);

        if (event == null) {
            return;
        }

        val address = event.getAddress();
        val command = new CreateOrderCommand(UUID.fromString(event.getBasketId()), address.getCountry(),
                address.getCity(), address.getStreet(), address.getHouse(), address.getApartment(), event.getVolume());

        val result = createOrderHandler.handle(command);
        if (result.isFailure()) {
            log.error("Order creation failed for basket {}: {}", event.getBasketId(), result.getError().serialize());
        }
    }

    private BasketEventsProto.BasketConfirmedIntegrationEvent parse(final byte[] payload) {
        try {
            return BasketEventsProto.BasketConfirmedIntegrationEvent.parseFrom(payload);
        } catch (InvalidProtocolBufferException | IllegalArgumentException ex) {
            log.warn("Skipping malformed basket event: {}", ex.getMessage());
            return null;
        }
    }
}
