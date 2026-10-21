package microarch.delivery.adapters.out.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import libs.ddd.DomainEvent;
import lombok.val;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedDomainEvent;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import queues.order.events.OrderEventsProto;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaDomainEventProducerTest {

    private static final String TOPIC = "order.events";

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, byte[]> kafkaTemplate = mock(KafkaTemplate.class);
    private final KafkaDomainEventProducer producer = new KafkaDomainEventProducer(kafkaTemplate);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(producer, "topic", TOPIC);
    }

    @Test
    @DisplayName("публикует OrderAssigned как protobuf-событие в топик order.events")
    void producesAssignedIntegrationEvent() throws InvalidProtocolBufferException {
        whenSendReturnsFuture();
        val order = newOrder();
        order.markAsAssigned();
        val event = (OrderAssignedDomainEvent) order.getDomainEvents().get(0);

        producer.produce(event);

        val sent = captureSent(event.getOrderId().toString());
        val parsed = OrderEventsProto.OrderAssignedIntegrationEvent.parseFrom(sent);
        assertThat(parsed.getOrderId()).isEqualTo(event.getOrderId().toString());
    }

    @Test
    @DisplayName("публикует OrderCompleted как protobuf-событие в топик order.events")
    void producesCompletedIntegrationEvent() throws InvalidProtocolBufferException {
        whenSendReturnsFuture();
        val order = newOrder();
        order.markAsAssigned();
        order.markAsCompleted();
        val event = (OrderCompletedDomainEvent) order.getDomainEvents().get(1);

        producer.produce(event);

        val sent = captureSent(event.getOrderId().toString());
        val parsed = OrderEventsProto.OrderCompletedIntegrationEvent.parseFrom(sent);
        assertThat(parsed.getOrderId()).isEqualTo(event.getOrderId().toString());
    }

    @Test
    @DisplayName("падает на неизвестном типе события")
    void failsOnUnknownEventType() {
        val unknown = new DomainEvent("test-source") {
        };

        assertThatThrownBy(() -> producer.produce(unknown)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown event type");
    }

    private Order newOrder() {
        return Order.create(UUID.randomUUID(), Location.create(4, 7).getValue(), Volume.create(3).getValue())
                .getValue();
    }

    private void whenSendReturnsFuture() {
        org.mockito.Mockito.when(kafkaTemplate.send(anyString(), anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    private byte[] captureSent(final String key) {
        val payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq(TOPIC), org.mockito.ArgumentMatchers.eq(key),
                payloadCaptor.capture());
        return payloadCaptor.getValue();
    }
}
