package microarch.delivery;

import libs.ddd.DomainEventPublisher;
import lombok.val;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.ports.DomainEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class DefaultDomainEventPublisherTest {

    private final DomainEventProducer producer = mock(DomainEventProducer.class);
    private final DomainEventPublisher publisher = new DefaultDomainEventPublisher(producer);

    @Test
    @DisplayName("отправляет события агрегата в producer и очищает их")
    void producesAggregateEventsAndClearsThem() {
        val order = Order.create(UUID.randomUUID(), Location.create(4, 7).getValue(), Volume.create(3).getValue())
                .getValue();
        order.markAsAssigned();

        publisher.publish(List.of(order));

        verify(producer).produce(any(OrderAssignedDomainEvent.class));
        assertThat(order.getDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("ничего не отправляет, если событий нет")
    void producesNothingWhenNoEvents() {
        val order = Order.create(UUID.randomUUID(), Location.create(4, 7).getValue(), Volume.create(3).getValue())
                .getValue();

        publisher.publish(List.of(order));

        verify(producer, never()).produce(any());
    }
}
