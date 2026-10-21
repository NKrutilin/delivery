package microarch.delivery.core.application.command.handler;

import lombok.val;
import libs.ddd.DomainEventPublisher;
import microarch.delivery.core.application.command.AssignOrderCommand;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.service.OrderAssignService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import libs.errs.Error;
import libs.errs.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AssignOrderHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final OrderAssignService orderAssignService = mock(OrderAssignService.class);
    private final DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);
    private final AssignOrderHandler handler = new AssignOrderHandler(orderRepository, courierRepository,
            orderAssignService, domainEventPublisher);

    @Test
    @DisplayName("ничего не делает, если нет заказов в статусе Created")
    void doesNothingWhenThereIsNoCreatedOrder() {
        when(orderRepository.getAnyInCreatedStatus()).thenReturn(Optional.empty());

        val result = handler.handle(new AssignOrderCommand());

        assertThat(result.isSuccess()).isTrue();
        verifyNoInteractions(orderAssignService, courierRepository);
    }

    @Test
    @DisplayName("назначает заказ курьеру и сохраняет изменения")
    void dispatchesOrderAndSavesChanges() {
        val order = Order
                .create(UUID.randomUUID(), Location.create(5, 5).getValueOrThrow(), Volume.create(3).getValueOrThrow())
                .getValueOrThrow();
        val courier = Courier.create("Ваня", Location.create(1, 1).getValueOrThrow()).getValueOrThrow();
        when(orderRepository.getAnyInCreatedStatus()).thenReturn(Optional.of(order));
        when(courierRepository.getAllCouriers()).thenReturn(List.of(courier));
        when(orderAssignService.assign(eq(order), any())).thenReturn(Result.success(courier));

        val result = handler.handle(new AssignOrderCommand());

        assertThat(result.isSuccess()).isTrue();
        verify(courierRepository).update(courier);
        verify(orderRepository).update(order);
        verify(domainEventPublisher).publish(List.of(order));
    }

    @Test
    @DisplayName("молча пропускает такт, если подходящих курьеров нет")
    void skipsSilentlyWhenNoCourierAvailable() {
        val order = Order
                .create(UUID.randomUUID(), Location.create(5, 5).getValueOrThrow(), Volume.create(3).getValueOrThrow())
                .getValueOrThrow();
        when(orderRepository.getAnyInCreatedStatus()).thenReturn(Optional.of(order));
        when(courierRepository.getAllCouriers()).thenReturn(List.of());
        when(orderAssignService.assign(eq(order), any()))
                .thenReturn(Result.failure(Error.of("assign.service.no.couriers", "No courier is available")));

        val result = handler.handle(new AssignOrderCommand());

        assertThat(result.isSuccess()).isTrue();
        verify(courierRepository, org.mockito.Mockito.never()).update(any());
        verify(orderRepository, org.mockito.Mockito.never()).update(any());
    }
}
