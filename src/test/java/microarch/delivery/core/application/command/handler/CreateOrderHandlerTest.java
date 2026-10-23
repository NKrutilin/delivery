package microarch.delivery.core.application.command.handler;

import lombok.val;
import microarch.delivery.core.application.command.CreateOrderCommand;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class CreateOrderHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CreateOrderHandler handler = new CreateOrderHandler(orderRepository);

    @Test
    @DisplayName("создаёт заказ со случайной локацией и сохраняет его")
    void createsOrderWithRandomLocation() {
        val command = new CreateOrderCommand(UUID.randomUUID(), "Россия", "Москва", "Тверская", "1", "2", 5);

        val result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        val captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).create(captor.capture());
        val saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(command.orderId());
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.Created);
        assertThat(saved.getVolume().getValue()).isEqualTo(5);
        assertThat(saved.getLocation().getX()).isBetween(1, 10);
        assertThat(saved.getLocation().getY()).isBetween(1, 10);
    }
}
