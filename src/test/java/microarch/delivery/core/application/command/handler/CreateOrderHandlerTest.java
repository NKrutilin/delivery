package microarch.delivery.core.application.command.handler;

import libs.errs.Error;
import libs.errs.Result;
import lombok.val;
import microarch.delivery.core.application.command.CreateOrderCommand;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.GeoService;
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
import static org.mockito.Mockito.when;

class CreateOrderHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final GeoService geoService = mock(GeoService.class);
    private final CreateOrderHandler handler = new CreateOrderHandler(orderRepository, geoService);

    @Test
    @DisplayName("создаёт заказ с локацией, полученной из Geo")
    void createsOrderWithLocationFromGeo() {
        val location = Location.create(4, 7).getValueOrThrow();
        when(geoService.getGeolocation("Тверская")).thenReturn(Result.success(location));
        val command = new CreateOrderCommand(UUID.randomUUID(), "Россия", "Москва", "Тверская", "1", "2", 5);

        val result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        val captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).create(captor.capture());
        val saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(command.orderId());
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.Created);
        assertThat(saved.getVolume().getValue()).isEqualTo(5);
        assertThat(saved.getLocation().getX()).isEqualTo(4);
        assertThat(saved.getLocation().getY()).isEqualTo(7);
    }

    @Test
    @DisplayName("возвращает ошибку, если Geo недоступен")
    void propagatesGeoFailure() {
        when(geoService.getGeolocation("Тверская"))
                .thenReturn(Result.failure(Error.of("geo.service.unavailable", "Geo is down")));
        val command = new CreateOrderCommand(UUID.randomUUID(), "Россия", "Москва", "Тверская", "1", "2", 5);

        val result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("geo.service.unavailable");
        verify(orderRepository, never()).create(any());
    }

    @Test
    @DisplayName("отклоняет заказ с нулевым объёмом")
    void rejectsNonPositiveVolume() {
        val command = new CreateOrderCommand(UUID.randomUUID(), "Россия", "Москва", "Тверская", "1", "2", 0);

        val result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
        verify(orderRepository, never()).create(any());
    }
}
