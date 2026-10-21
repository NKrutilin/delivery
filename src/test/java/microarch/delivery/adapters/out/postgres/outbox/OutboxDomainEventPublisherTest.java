package microarch.delivery.adapters.out.postgres.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedDomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class OutboxDomainEventPublisherTest {

    private final OutboxJpaRepository jpa = mock(OutboxJpaRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final OutboxDomainEventPublisher publisher = new OutboxDomainEventPublisher(jpa, objectMapper);

    @Test
    @DisplayName("сохраняет каждое событие агрегата в outbox и очищает агрегат")
    void savesEachRaisedEventToOutboxAndClearsAggregate() throws Exception {
        val order = Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(), Volume.create(10).getValue())
                .getValue();
        assertThat(order.markAsAssigned().isSuccess()).isTrue();
        assertThat(order.markAsCompleted().isSuccess()).isTrue();

        publisher.publish(List.of(order));

        val captor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(jpa, times(2)).save(captor.capture());

        val assigned = captor.getAllValues().get(0);
        assertThat(assigned.getEventType()).isEqualTo(OrderAssignedDomainEvent.class.getName());
        assertThat(assigned.getAggregateId()).isEqualTo(order.getId().toString());
        assertThat(assigned.getAggregateType()).isEqualTo("Order");
        assertThat(assigned.getProcessedOnUtc()).isNull();
        val restored = objectMapper.readValue(assigned.getPayload(), OrderAssignedDomainEvent.class);
        assertThat(restored.getOrderId()).isEqualTo(order.getId());

        val completed = captor.getAllValues().get(1);
        assertThat(completed.getEventType()).isEqualTo(OrderCompletedDomainEvent.class.getName());
        assertThat(order.getDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("ничего не сохраняет, если событий нет")
    void savesNothingWhenAggregateHasNoEvents() {
        val order = Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(), Volume.create(10).getValue())
                .getValue();

        publisher.publish(List.of(order));

        verifyNoInteractions(jpa);
    }
}
