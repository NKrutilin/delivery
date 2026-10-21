package microarch.delivery.core.application.command.handler;

import lombok.val;
import libs.ddd.DomainEventPublisher;
import microarch.delivery.core.application.command.CompleteOrderCommand;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Status;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompleteOrderHandlerTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);
    private final CompleteOrderHandler handler = new CompleteOrderHandler(courierRepository, orderRepository,
            domainEventPublisher);

    @Test
    @DisplayName("завершает задание курьера и помечает заказ выполненным")
    void completesOrderWhenCourierNearby() {
        val location = Location.create(2, 3).getValueOrThrow();
        val order = Order.create(UUID.randomUUID(), location, Volume.create(3).getValueOrThrow()).getValueOrThrow();
        val courier = Courier.create("Ваня", location).getValueOrThrow();
        courier.takeOrder(order.getId(), order.getVolume(), order.getLocation()).getValueOrThrow();
        order.markAsAssigned().getValueOrThrow();
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        val result = handler.handle(new CompleteOrderCommand(courier.getId(), order.getId()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getAssignments()).hasSize(1);
        assertThat(courier.getAssignments().get(0).getStatus()).isEqualTo(Status.Completed);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.Completed);
        verify(courierRepository).update(courier);
        verify(orderRepository).update(order);
        verify(domainEventPublisher).publish(List.of(order));
    }

    @Test
    @DisplayName("возвращает ошибку, если курьер не найден")
    void failsWhenCourierDoesNotExist() {
        when(courierRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        val result = handler.handle(new CompleteOrderCommand(UUID.randomUUID(), UUID.randomUUID()));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.not.found");
    }
}
