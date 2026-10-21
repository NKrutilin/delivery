package microarch.delivery.adapters.out.postgres.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import libs.ddd.DomainEvent;
import lombok.val;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.ports.DomainEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobTest {

    private final DomainEventProducer producer = mock(DomainEventProducer.class);
    private final OutboxJpaRepository jpa = mock(OutboxJpaRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final Job job = new Job(producer, jpa, objectMapper);

    private OutboxMessage createMessage(final OrderAssignedDomainEvent event) throws Exception {
        return new OutboxMessage(event.getEventId(), event.getClass().getName(), event.getOrderId().toString(),
                Order.class.getSimpleName(), objectMapper.writeValueAsString(event), event.getOccurredOnUtc());
    }

    private Order newOrder() {
        return Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(), Volume.create(10).getValue())
                .getValue();
    }

    @Test
    @DisplayName("восстанавливает событие из payload, публикует и помечает сообщение обработанным")
    void restoresEventFromPayloadPublishesItAndMarksMessageProcessed() throws Exception {
        val order = newOrder();
        val message = createMessage(new OrderAssignedDomainEvent(order));
        when(jpa.findUnprocessedMessages()).thenReturn(List.of(message));

        job.run();

        val eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(producer).produce(eventCaptor.capture());
        val produced = assertInstanceOf(OrderAssignedDomainEvent.class, eventCaptor.getValue());
        assertThat(produced.getOrderId()).isEqualTo(order.getId());

        verify(jpa).save(message);
        assertThat(message.getProcessedOnUtc()).isNotNull();
    }

    @Test
    @DisplayName("оставляет сообщение необработанным при ошибке публикации, чтобы повторить попытку")
    void keepsMessageUnprocessedWhenProducerFailsSoItWillBeRetried() throws Exception {
        val order = newOrder();
        val message = createMessage(new OrderAssignedDomainEvent(order));
        when(jpa.findUnprocessedMessages()).thenReturn(List.of(message));
        doThrow(new RuntimeException("kafka down")).when(producer).produce(any(DomainEvent.class));

        job.run();

        verify(jpa, never()).save(any(OutboxMessage.class));
        assertThat(message.getProcessedOnUtc()).isNull();
    }
}
